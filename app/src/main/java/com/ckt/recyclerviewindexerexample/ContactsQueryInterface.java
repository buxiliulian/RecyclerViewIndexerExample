package com.ckt.recyclerviewindexerexample;

import android.provider.ContactsContract;

public interface ContactsQueryInterface {
    String[] PROJECTION = {
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
            ContactsContract.Contacts.LOOKUP_KEY,
            ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,
            ContactsContract.Contacts.SORT_KEY_PRIMARY
    };
    int INDEX_ID = 0;
    int INDEX_NAME = 1;
    int INDEX_LOOKUP_KEY = 2;
    int INDEX_PHOTO = 3;
    int INDEX_SORT_KEY = 4;
}
