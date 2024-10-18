To use the MCP and test clients
1. Find the locations.txt file (If it does not exist ignore instructions 1 to 2)
2. Input the IP, Port and location of the checkpoint and stations in this format IPPort_location
There should be no space between the IP and port and an underscore between the location and port.
3. Get all the testClients from the clients branch.
4. Setup the checkpoints you want to initialise in the inputs.txt file (They should match the locations.txt file)
5. The format of the inputs.txt file is a so: TYPE:ID:PORT E.g. CP:01:3002
6. Remember that the MCP uses port 3001 so dont double up.
7. ONLY make checkpoints, do not make any stations in the current configuration checkpoint 2 is hardcoded as a station.
8. Start the MCP and a GUI should appear on screen and it should enter the WAITING state
9. Start the test clients with any number of bladerunners (no more that 5) a test clients GUI should appear
10. All checkpoints will autoload according to the inputs.txt file
11. If the checkpoints do no connect change the IP address in the test clients (line 45 in the Main.java file)
12. Once all clients have been connected in the MCP GUI type "start mapping" the CLI is located below the log output, the MCP should enter the MAPPING state
13. To trip a checkpoint type "cp <ID>" where ID is the id of the checkpoint
14. Every bladerunner needs to be mapped before a the MCP will enter the RUNNING state
15. To map a bladerunner a checkpoint needs to be tripped twice, once to get the bladerunner "ON" it then another time to get it "OFF" it.
16. For any further questions contact me through discord, Rex has my discord, my account is TNTsuper