package de.presti.osz.arl.utils;

import javax.swing.*;
import java.io.OutputStream;

public class LogOutputStream extends OutputStream {

    private final JTextArea jTextArea;

    public LogOutputStream(JTextArea jTextArea) {
        this.jTextArea = jTextArea;
    }

    public void write(int b) {
        this.jTextArea.append(String.valueOf((char)b));
        this.jTextArea.setCaretPosition(this.jTextArea.getDocument().getLength());
    }

}
