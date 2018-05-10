package org.thoughtcrime.securesms.glide;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;

import org.thoughtcrime.securesms.util.Conversions;

import java.io.InputStream;

public class KeyedInputStreamLoader implements ModelLoader<KeyedInputStream, InputStream> {
  @Nullable
  @Override
  public LoadData<InputStream> buildLoadData(@NonNull KeyedInputStream keyedInputStream, int width, int height, @NonNull Options options) {
    return new LoadData<>(keyedInputStream, new KeyedInputStreamFetcher(keyedInputStream));
  }

  @Override
  public boolean handles(@NonNull KeyedInputStream inputStream) {
    return true;
  }

  public static class Factory implements ModelLoaderFactory<KeyedInputStream, InputStream> {

    @NonNull
    @Override
    public ModelLoader<KeyedInputStream, InputStream> build(@NonNull MultiModelLoaderFactory multiFactory) {
      return new KeyedInputStreamLoader();
    }

    @Override
    public void teardown() { }
  }
}
