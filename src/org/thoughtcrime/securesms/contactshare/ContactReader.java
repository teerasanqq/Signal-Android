package org.thoughtcrime.securesms.contactshare;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.thoughtcrime.securesms.glide.KeyedInputStream;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ContactReader {

  private final InputStream inputStream;
  private final Contact     contact;

  public ContactReader(@NonNull InputStream inputStream) throws IOException {
    this.inputStream = inputStream;

    DataInputStream dataStream    = new DataInputStream(inputStream);
    int             version       = dataStream.readInt();

    if (version != ContactStream.VERSION) {
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

    contact = Contact.deserialize(new String(contactBytes));

    if (contact == null) {
      throw new IOException("Failed to parse the contact.");
    }
  }

  public Contact getContact() {
    return contact;
  }

  public @Nullable InputStream getAvatar() throws IOException {
    if (inputStream.available() > 0) {
      return new KeyedInputStream(String.valueOf(contact.hashCode()), inputStream);
    }
    return null;
  }
}
