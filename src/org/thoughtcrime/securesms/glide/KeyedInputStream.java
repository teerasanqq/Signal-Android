package org.thoughtcrime.securesms.glide;

import android.support.annotation.NonNull;

import com.bumptech.glide.load.Key;

import java.io.FilterInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

public class KeyedInputStream extends FilterInputStream implements Key {

  private final String key;

  public KeyedInputStream(@NonNull String key, @NonNull InputStream stream) {
    super(stream);
    this.key = key;
  }

  @Override
  public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
    messageDigest.update(key.getBytes());
  }

  public @NonNull String getKey() {
    return key;
  }
}
