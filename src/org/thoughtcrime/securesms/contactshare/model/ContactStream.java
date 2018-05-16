package org.thoughtcrime.securesms.contactshare.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.thoughtcrime.securesms.util.Conversions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.SequenceInputStream;
import java.util.Arrays;
import java.util.Collections;

public final class ContactStream extends InputStream {

  public static final int VERSION = 1;

  private final InputStream inputStream;

  public ContactStream(@NonNull Contact contact, @Nullable InputStream attachmentStream) throws IOException {
    byte[]      contactBytes        = contact.serialize().getBytes();
    byte[]      contactHeader       = Conversions.intToByteArray(contactBytes.length);
    byte[]      version             = Conversions.intToByteArray(VERSION);
    InputStream contactStream       = new ByteArrayInputStream(contactBytes);
    InputStream contactHeaderStream = new ByteArrayInputStream(contactHeader);
    InputStream versionStream       = new ByteArrayInputStream(version);

    if (attachmentStream != null) {
      inputStream = new SequenceInputStream(Collections.enumeration(Arrays.asList(versionStream, contactHeaderStream, contactStream, attachmentStream)));
    } else {
      inputStream = new SequenceInputStream(Collections.enumeration(Arrays.asList(versionStream, contactHeaderStream, contactStream)));
    }
  }

  @Override
  public int read() throws IOException {
    return inputStream.read();
  }

  @Override
  public int read(@NonNull byte[] b) throws IOException {
    return inputStream.read(b);
  }

  @Override
  public int read(@NonNull byte[] b, int off, int len) throws IOException {
    return inputStream.read(b, off, len);
  }

  @Override
  public long skip(long n) throws IOException {
    return inputStream.skip(n);
  }

  @Override
  public int available() throws IOException {
    return inputStream.available();
  }

  @Override
  public void close() throws IOException {
    inputStream.close();
  }
}
