package org.example;

public class StartupState {
    private final Database db = Database.getInstance();
    private int maxTrains = 5;
    private int maxStations = 5;
    private int maxCheckpoints = 10;
    private int currentTrain = 0;
    private CurrentTrainInfo currentTrainInfo = null;

    public boolean performOperation() {
        // Check if the required number of trains, stations, and checkpoints are connected
        int curTrains = db.getTrainCount();
        int curStations = db.getStationCount();
        int curCheckpoints = db.getCheckpointCount();

        // Wait until all required clients are connected
        if (curTrains == maxTrains && curStations == maxStations && curCheckpoints == maxCheckpoints) {
            return startMapping(); // Start mapping process once all clients are connected
        }

        return false;
    }

    private boolean startMapping() {
        if (currentTrainInfo == null || currentTrainInfo.process()) {
            currentTrain++;
            if (currentTrain > maxTrains) {
                return true;
            }

            currentTrainInfo = new CurrentTrainInfo(db.getTrain(String.valueOf(currentTrain)));
        }
        return false;
    }

    private class CurrentTrainInfo {
        private final TrainClient train;
        private boolean hasSent;
        private long timeSinceSent;

        public CurrentTrainInfo(TrainClient train) {
            this.train = train;
        }

        private boolean process() {
            if (!hasSent) {
                sendTrainToNextCheckpoint();
            } else if (System.currentTimeMillis() - timeSinceSent > 5000) {
                retrySending();
            } else {
                CheckpointClient hitClient = db.getHit();
                if (hitClient != null) {
                    stopTrainAtCheckpoint(hitClient);
                    hitClient.reset();
                    return true;
                }
            }

            return false;
        }

        private void sendTrainToNextCheckpoint() {
            long tempTime = System.currentTimeMillis();
            String message = MessageGenerator.generateExecuteMessage("ccp", train.id, tempTime, 1);
            train.sendMessage(message);
            timeSinceSent = tempTime;
            hasSent = true;
        }

        private void stopTrainAtCheckpoint(CheckpointClient hitClient) {
            String message = MessageGenerator.generateExecuteMessage("ccp", train.id, System.currentTimeMillis(), 0);
            train.sendMessage(message);
            train.changeZone(hitClient.getLocation());
            hasSent = true;
        }

        private void retrySending() {
            hasSent = false; // Retry sending the message if no response within 10 seconds
            sendTrainToNextCheckpoint();
        }
    }
}
