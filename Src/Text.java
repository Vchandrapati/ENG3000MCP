package Src;

// Java program to create a blank text 
// field of definite number of columns.
import java.awt.event.*;
import javax.swing.*;

class Text extends JFrame implements ActionListener, KeyListener {
    // JTextField
    private JTextField t;

    // JFrame
    private JFrame f;

    // JButton
    private JButton b;

    // label to display text
    private JLabel l;

    private String fullText = "";

    private Main main;

    // default constructor
    public Text(Main m) {
        main = m;
        // create a new frame to store text field and button
        f = new JFrame("textfield");

        // create a label to display text
        l = new JLabel("");

        // create a new button
        b = new JButton("submit");

        // addActionListener to button
        b.addActionListener(this);

        // create a object of JTextField with 16 columns
        t = new JTextField(16);

        // add keylistener to textfield
        t.addKeyListener(this);

        // create a panel to add buttons and textfield
        JPanel p = new JPanel();

        // add buttons and textfield to panel
        p.add(t);
        p.add(b);
        p.add(l);

        // add panel to frame
        f.add(p);

        // set the size of frame
        f.setSize(280, 300);
        f.setLocationRelativeTo(null);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setAlwaysOnTop(true);
        f.setResizable(false);
    }

    public void display() {
        f.setVisible(true);
        f.requestFocusInWindow();
    }

    public void submit() {
        // set the text of the label to the text of the field
        String text = t.getText();
        System.out.println(text);
        main.Commands(text);

        fullText = fullText + text + "<br/>";

        l.setText("<html>" + fullText + "</html>");

        // set the text of field to blank
        t.setText("");

    }

    // if the button is pressed
    public void actionPerformed(ActionEvent e) {
        String s = e.getActionCommand();
        if (s.equals("submit")) {
            submit();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        if (e.getKeyChar() == KeyEvent.VK_ENTER) {
            submit();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}