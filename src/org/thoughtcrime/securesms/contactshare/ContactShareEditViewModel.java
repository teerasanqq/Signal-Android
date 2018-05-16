package org.thoughtcrime.securesms.contactshare;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.annimon.stream.Stream;

import org.thoughtcrime.securesms.util.SingleLiveEvent;

import java.util.ArrayList;
import java.util.List;

class ContactShareEditViewModel extends ViewModel {

  private final MutableLiveData<List<ContactWithAvatar>> contactsWithAvatars;
  private final SingleLiveEvent<Event>                   events;
  private final ContactRepository                        repo;

  ContactShareEditViewModel(@NonNull List<Long>        contactIds,
                            @NonNull ContactRepository contactRepository)
  {
    contactsWithAvatars = new MutableLiveData<>();
    events              = new SingleLiveEvent<>();
    repo                = contactRepository;

    repo.getContactsWithAvatars(contactIds, retrieved -> {
      if (retrieved.isEmpty()) {
        events.postValue(Event.BAD_CONTACT);
      } else {
        contactsWithAvatars.postValue(retrieved);
      }
    });
  }

  @NonNull LiveData<List<ContactWithAvatar>> getContactsWithAvatars() {
    return contactsWithAvatars;
  }

  @NonNull List<ContactWithAvatar> getFinalizedContacts() {
    List<ContactWithAvatar> currentContacts = getCurrentContacts();
    List<ContactWithAvatar> trimmedContacts = new ArrayList<>(currentContacts.size());

    for (ContactWithAvatar contact : currentContacts) {
      Contact trimmed = new Contact(contact.getContact().getName(),
                                    contact.getContact().getOrganization(),
                                    trimSelectables(contact.getContact().getPhoneNumbers()),
                                    trimSelectables(contact.getContact().getEmails()),
                                    trimSelectables(contact.getContact().getPostalAddresses()),
                                    contact.getContact().getAvatarState(),
                                    contact.getContact().getAvatarSize(),
                                    contact.getContact().getAttachmentId());
      trimmedContacts.add(new ContactWithAvatar(trimmed, contact.getAvatarAttachment()));
    }

    return trimmedContacts;
  }

  @NonNull LiveData<Event> getEvents() {
    return events;
  }

  private <E extends Selectable> List<E> trimSelectables(List<E> selectables) {
    return Stream.of(selectables).filter(Selectable::isSelected).toList();
  }

  @NonNull
  private List<ContactWithAvatar> getCurrentContacts() {
    List<ContactWithAvatar> currentContacts = contactsWithAvatars.getValue();
    return currentContacts != null ? currentContacts : new ArrayList<>();
  }

  enum Event {
    BAD_CONTACT
  }

  static class Factory extends ViewModelProvider.NewInstanceFactory {

    private final List<Long>        contactIds;
    private final ContactRepository contactRepository;

    Factory(@NonNull List<Long> contactIds, @NonNull ContactRepository contactRepository) {
      this.contactIds        = contactIds;
      this.contactRepository = contactRepository;
    }

    @Override
    public @NonNull <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
      return modelClass.cast(new ContactShareEditViewModel(contactIds, contactRepository));
    }
  }
}
