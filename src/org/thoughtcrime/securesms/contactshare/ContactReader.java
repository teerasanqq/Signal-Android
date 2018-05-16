package org.thoughtcrime.securesms.contactshare;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.thoughtcrime.securesms.glide.KeyedInputStream;
import org.thoughtcrime.securesms.util.Util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class ContactReader {

  private final InputStream inputStream;
  private final Contact     contact;

  public ContactReader(@NonNull InputStream inputStream) throws IOException {
    this.inputStream = inputStream;

    int version = readInt(inputStream);

    if (version != ContactStream.VERSION) {
      throw new IOException("Unexpected version: " + version);
    }

    int contactLength = readInt(inputStream);

    if (contactLength <= 0) {
      throw new IOException("Invalid contact length: " + contactLength);
    }

    byte[] contactBytes = new byte[contactLength];
    Util.readFully(inputStream, contactBytes);

    contact = Contact.deserialize(new String(contactBytes));

    if (contact == null) {
      throw new IOException("Failed to parse the contact.");
    }
  }

  public Contact getContact() {
    return contact;
  }

  public @Nullable KeyedInputStream getAvatar() throws IOException {
    if (inputStream.available() > 0) {
      return new KeyedInputStream(String.valueOf(contact.hashCode()), inputStream);
    }
    return null;
  }

  private static int readInt(@NonNull InputStream inputStream) throws  IOException {
    byte[] buffer = new byte[4];
    Util.readFully(inputStream, buffer);
    return ByteBuffer.wrap(buffer).getInt();
  }
}
