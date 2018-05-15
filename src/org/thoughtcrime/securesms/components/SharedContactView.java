package org.thoughtcrime.securesms.components;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.annimon.stream.Stream;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.contactshare.ContactRepository.ContactInfo;
import org.thoughtcrime.securesms.contactshare.ContactUtil;
import org.thoughtcrime.securesms.contactshare.SharedContactInjector;
import org.thoughtcrime.securesms.contactshare.model.Contact;
import org.thoughtcrime.securesms.contactshare.model.Phone;
import org.thoughtcrime.securesms.database.RecipientDatabase;
import org.thoughtcrime.securesms.mms.GlideRequests;
import org.thoughtcrime.securesms.mms.SharedContactSlide;
import org.thoughtcrime.securesms.recipients.Recipient;
import org.thoughtcrime.securesms.recipients.RecipientModifiedListener;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SharedContactView extends LinearLayout implements SharedContactInjector.Target,
                                                               RecipientModifiedListener {

  private ImageView avatarView;
  private TextView  nameView;
  private TextView  numberView;
  private TextView  actionButtonView;
  private ViewGroup actionButtonContainerView;

  private SharedContactSlide sharedContactSlide;
  private Contact            contact;
  private Locale             locale;
  private GlideRequests      glideRequests;
  private EventListener      eventListener;

  private final Map<String, Recipient> activeRecipients = new HashMap<>();

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

  public void setContact(@NonNull SharedContactSlide sharedContactSlide, @NonNull GlideRequests glideRequests, @NonNull Locale locale) {
    this.sharedContactSlide = sharedContactSlide;
    this.glideRequests      = glideRequests;
    this.locale             = locale;

    Stream.of(activeRecipients.values()).forEach(recipient ->  recipient.removeListener(this));
    this.activeRecipients.clear();

    SharedContactInjector.getInstance(getContext()).load(sharedContactSlide, this);
  }

  @Override
  public void setContact(@Nullable Contact contact) {
    this.contact = contact;

    if (contact != null) {
      nameView.setText(ContactUtil.getDisplayName(contact));

      Phone displayNumber = ContactUtil.getDisplayNumber(contact);

      // TODO(greyson): Make one big util?
      if (displayNumber != null) {
        numberView.setText(ContactUtil.getPrettyPhoneNumber(displayNumber, locale));
      } else if (contact.getEmails().size() > 0) {
        numberView.setText(contact.getEmails().get(0).getEmail());
      } else {
        numberView.setText("");
      }

      presentActionButtons(ContactUtil.getRecipients(getContext(), contact));
    } else {
      nameView.setText("");
      numberView.setText("");
    }
  }

  @Override
  public void setAvatar(@Nullable InputStream inputStream) {
    if (inputStream != null) {
      glideRequests.load(inputStream)
                   .fallback(R.drawable.ic_contact_picture)
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

  @Override
  public void onModified(Recipient recipient) {
    presentActionButtons(Collections.singletonList(recipient));
  }

  public void setEventListener(@NonNull EventListener eventListener) {
    this.eventListener = eventListener;
  }

  private void presentActionButtons(@NonNull List<Recipient> recipients) {
    for (Recipient recipient : recipients) {
      activeRecipients.put(recipient.getAddress().serialize(), recipient);
    }

    // TODO(greyson): Make one forloop to go through this
    Stream.of(activeRecipients.values()).forEach(recipient -> { recipient.removeListener(this); recipient.addListener(this);});

    List<Recipient> pushUsers   = Stream.of(activeRecipients.values())
                                        .filter(recipient -> recipient.getRegistered() == RecipientDatabase.RegisteredState.REGISTERED)
                                        .toList();

    List<Recipient> systemUsers = Stream.of(activeRecipients.values())
                                        .filter(recipient -> recipient.getRegistered() != RecipientDatabase.RegisteredState.REGISTERED && recipient.isSystemContact())
                                        .toList();

    if (!pushUsers.isEmpty()) {
      actionButtonView.setText(R.string.SharedContactView_message);
      actionButtonView.setOnClickListener(v -> {
        if (eventListener != null) {
          eventListener.onMessageClicked(pushUsers);
        }
      });
    } else if (!systemUsers.isEmpty()) {
      actionButtonView.setText(R.string.SharedContactView_invite_to_signal);
      actionButtonView.setOnClickListener(v -> {
        if (eventListener != null) {
          eventListener.onInviteClicked(systemUsers);
        }
      });
    } else {
      actionButtonView.setText(R.string.SharedContactView_add_to_contacts);
      actionButtonView.setOnClickListener(v -> {
        if (eventListener != null) {
          eventListener.onAddToContactsClicked(sharedContactSlide, contact);
        }
      });
    }
  }

  public @NonNull View getAvatarView() {
    return avatarView;
  }

  public interface EventListener {
    void onAddToContactsClicked(@NonNull SharedContactSlide sharedContactSlide, @NonNull Contact contact);
    void onInviteClicked(@NonNull List<Recipient> choices);
    void onMessageClicked(@NonNull List<Recipient> choices);
  }
}
