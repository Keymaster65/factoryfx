package de.factoryfx.javafx.javascript.editor.attribute.visualisation;

import java.io.IOException;
import java.io.OutputStream;

public class DiscardOutputStream extends OutputStream {

    @Override
    public void write(byte[] b) throws IOException {
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
    }

    @Override
    public void write(int b) throws IOException {
    }
}
