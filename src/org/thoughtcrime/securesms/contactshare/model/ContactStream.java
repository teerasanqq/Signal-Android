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

// TODO(greyson): Break this into two separate classes, probably
public final class ContactStream {

  private ContactStream() { }

  public static class Stream extends InputStream {

    private static final int VERSION = 1;

    final InputStream inputStream;

    public Stream(@NonNull Contact contact, @Nullable InputStream attachmentStream) throws IOException {
      ByteArrayOutputStream contactByteStream = new ByteArrayOutputStream();
      ObjectOutputStream    contactWriter     = new ObjectOutputStream(contactByteStream);
      contactWriter.writeObject(contact);
      contactWriter.close();

      byte[]      contactBytes        = contactByteStream.toByteArray();
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

  public static class Reader {

    private final InputStream inputStream;
    private final Contact     contact;

    public Reader(@NonNull InputStream inputStream) throws IOException {
      this.inputStream = inputStream;

      DataInputStream dataStream    = new DataInputStream(inputStream);
      int             version       = dataStream.readInt();

      if (version != 1) {
        throw new IOException("Unexpected version: " + version);
      }

      int contactLength = dataStream.readInt();

      if (contactLength <= 0) {
        throw new IOException("Invalid contact length: " + contactLength);
      }

      byte[] contactBytes = new byte[contactLength];

      int totalRead = 0;
      int read      = 0;
      while ((read = inputStream.read(contactBytes, totalRead, contactBytes.length - totalRead)) > 0 && totalRead < contactBytes.length) {
        totalRead += read;
      }

      if (totalRead < contactBytes.length) {
        throw new IOException("Failed to read all of the contact bytes.");
      }

      try {
        contact = (Contact) new ObjectInputStream(new ByteArrayInputStream(contactBytes)).readObject();
      } catch (ClassNotFoundException e) {
        throw new IOException("Failed to parse contact.", e);
      }
    }

    public Contact getContact() {
      return contact;
    }

    public @Nullable InputStream getAvatar() throws IOException {
      if (inputStream.available() > 0) {
        return inputStream;
      }
      return null;
    }
  }
}
