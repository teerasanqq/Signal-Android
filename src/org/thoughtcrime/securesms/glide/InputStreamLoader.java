package org.thoughtcrime.securesms.glide;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;

import org.thoughtcrime.securesms.util.Conversions;

import java.io.InputStream;

public class InputStreamLoader implements ModelLoader<InputStream, InputStream> {
  @Nullable
  @Override
  public LoadData<InputStream> buildLoadData(@NonNull InputStream inputStream, int width, int height, @NonNull Options options) {
    return new LoadData<InputStream>(messageDigest -> messageDigest.update(Conversions.intToByteArray(inputStream.hashCode())),
                                     new InputStreamFetcher(inputStream));
  }

  @Override
  public boolean handles(@NonNull InputStream inputStream) {
    return true;
  }

  public static class Factory implements ModelLoaderFactory<InputStream, InputStream> {

    @NonNull
    @Override
    public ModelLoader<InputStream, InputStream> build(@NonNull MultiModelLoaderFactory multiFactory) {
      return new InputStreamLoader();
    }

    @Override
    public void teardown() {

    }
  }
}
