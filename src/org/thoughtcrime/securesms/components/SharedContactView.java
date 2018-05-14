package org.thoughtcrime.securesms.components;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.contactshare.ContactRepository.ContactInfo;
import org.thoughtcrime.securesms.contactshare.ContactUtil;
import org.thoughtcrime.securesms.contactshare.SharedContactInjector;
import org.thoughtcrime.securesms.contactshare.SharedContactViewModel.ContactViewDetails;
import org.thoughtcrime.securesms.contactshare.model.Contact;
import org.thoughtcrime.securesms.contactshare.model.Phone;
import org.thoughtcrime.securesms.mms.GlideRequest;
import org.thoughtcrime.securesms.mms.GlideRequests;
import org.thoughtcrime.securesms.mms.PartAuthority;
import org.thoughtcrime.securesms.mms.SharedContactSlide;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

public class SharedContactView extends LinearLayout implements SharedContactInjector.Target {

  private ImageView avatarView;
  private TextView  nameView;
  private TextView  numberView;
  private TextView  actionButtonView;
  private ViewGroup actionButtonContainerView;

  private Locale        locale;
  private GlideRequests glideRequests;
  private EventListener eventListener;

  public SharedContactView(Context context) {
    super(context);
    initialize();
  }

  public SharedContactView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    initialize();
  }

  public SharedContactView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    initialize();
  }

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  public SharedContactView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    initialize();
  }

  private void initialize() {
    inflate(getContext(), R.layout.shared_contact_view, this);

    avatarView                = findViewById(R.id.contact_avatar);
    nameView                  = findViewById(R.id.contact_name);
    numberView                = findViewById(R.id.contact_number);
    actionButtonView          = findViewById(R.id.contact_action_button);
    actionButtonContainerView = findViewById(R.id.contact_action_button_container);
  }

  public void setContact(@NonNull SharedContactSlide contactSlide, @NonNull GlideRequests glideRequests, @NonNull Locale locale) {
    this.glideRequests = glideRequests;
    this.locale        = locale;

    nameView.setText("We're getting closer!");
    numberView.setText("610-555-5555");

    glideRequests.load(R.drawable.ic_contact_picture)
        .circleCrop()
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .into(avatarView);

    SharedContactInjector.getInstance(getContext()).load(contactSlide, this);

//    Contact contact       = contactDetails.getContactInfo().getContact();
//    Phone   displayNumber = ContactUtil.getDisplayNumber(contactDetails.getContactInfo());
//
//    presentHeader(contact, displayNumber, glideRequests, locale);
//
//    switch (contactDetails.getState()) {
//      case NEW:
//        presentNewContactState(contact);
//        break;
//      case ADDED:
//        presentAddedContactState(contactDetails.getContactInfo(), displayNumber);
//        break;
//    }
  }

  private void presentHeader(@NonNull Contact contact, @Nullable Phone displayNumber, @NonNull GlideRequests glideRequests, @NonNull Locale locale) {
//    if (contact.getAvatarState() != null) {
//      try {
//        glideRequests.load(contact.getAvatarState().getImageStream(getContext()))
//                     .fallback(R.drawable.ic_contact_picture)
//                     .circleCrop()
//                     .diskCacheStrategy(DiskCacheStrategy.ALL)
//                     .into(avatarView);
//      } catch (IOException e) {
//        // TODO: Do better
//        e.printStackTrace();
//      }
//    } else {
      glideRequests.load(R.drawable.ic_contact_picture)
                   .circleCrop()
                   .diskCacheStrategy(DiskCacheStrategy.ALL)
                   .into(avatarView);
//    }

    nameView.setText(ContactUtil.getDisplayName(contact));

    if (displayNumber != null) {
      numberView.setText(ContactUtil.getPrettyPhoneNumber(displayNumber, locale));
    } else if (contact.getEmails().size() > 0) {
      numberView.setText(contact.getEmails().get(0).getEmail());
    } else {
      numberView.setText("");
    }
  }

  private void presentNewContactState(@NonNull Contact contact) {
    actionButtonView.setText(R.string.SharedContactView_add_to_contacts);
    actionButtonView.setOnClickListener(v -> {
      if (eventListener != null) {
        eventListener.onAddToContactsClicked(contact);
      }
    });
  }

  private void presentAddedContactState(@NonNull ContactInfo contactInfo, @Nullable Phone displayNumber) {
    if (displayNumber == null) {
      actionButtonContainerView.setVisibility(GONE);
      return;
    }

    actionButtonContainerView.setVisibility(VISIBLE);

    if (contactInfo.isPush(displayNumber)) {
      actionButtonView.setText(R.string.SharedContactView_message);

      actionButtonView.setOnClickListener(v -> {
        if (eventListener != null) {
          eventListener.onMessageClicked(displayNumber);
        }
      });
    } else {
      actionButtonView.setText(R.string.SharedContactView_invite_to_signal);

      actionButtonView.setOnClickListener(v -> {
        if (eventListener != null) {
          eventListener.onInviteClicked(displayNumber);
        }
      });
    }
  }

  public View getAvatarView() {
    return avatarView;
  }

  public void setEventListener(EventListener listener) {
    this.eventListener = listener;
  }

  @Override
  public void setContact(@Nullable Contact contact) {
    if (contact != null) {
      nameView.setText(contact.getName().getDisplayName());
      numberView.setText("We'll do that next");
    } else {
      nameView.setText("");
      numberView.setText("");
    }
  }

  @Override
  public void setAvatar(@Nullable InputStream inputStream) {
    if (inputStream != null) {
      Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

      glideRequests.load(bitmap)
                   .circleCrop()
                   .diskCacheStrategy(DiskCacheStrategy.ALL)
                   .into(avatarView);
    } else {
      glideRequests.load(R.drawable.ic_contact_picture)
                   .circleCrop()
                   .diskCacheStrategy(DiskCacheStrategy.ALL)
                   .into(avatarView);
    }
  }

  public interface EventListener {
    void onAddToContactsClicked(@NonNull Contact contact);
    void onInviteClicked(@NonNull Phone phoneNumber);
    void onMessageClicked(@NonNull Phone phoneNumber);
  }
}
