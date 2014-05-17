package org.riking.mctesting;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

public class NullOutputStream extends OutputStream {

    @Override
    public void write(int i) { }

    @Override
    public void write(byte[] b) { }

    @Override
    public void write(byte[] b, int off, int len) { }

    @Override
    public void flush() { }

    @Override
    public void close() { }
}
