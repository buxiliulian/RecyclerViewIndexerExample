package com.ckt.recyclerviewindexer;

import android.database.Cursor;
import android.widget.AlphabetIndexer;

/**
 * A helper class for adapters that implement the SectionIndexer interface based on {@link android.widget.SectionIndexer}.
 * <p>
 * If the items in the adapter are sorted by simple ASCII-based sorting of contacts database,
 * then this class provides a way to do fast indexing of large lists using binary search.
 * <p>
 * Your adapter is responsible for updating the cursor by calling setCursor() if the
 * cursor changes. getPositionForSection() method does the binary search for the starting
 * index of a given section (alphabet).
 *
 * @author David Chow
 */
public class ContactsIndexer extends AlphabetIndexer {

    public static final String DEFAULT_INDEXER_CHARACTERS = "%ABCDEFGHIJKLMNOPQRSTUVWXYZ#";
    private String mContactsIndexer;
    private int mAlphabetLength;

    /**
     * Constructs the indexer.
     *
     * @param cursor            the cursor containing the data set
     * @param sortedColumnIndex the column number in the cursor that is sorted
     *                          alphabetically
     */
    public ContactsIndexer(Cursor cursor, int sortedColumnIndex) {
        this(cursor, sortedColumnIndex, DEFAULT_INDEXER_CHARACTERS);
    }

    /**
     * Constructs the indexer.
     *
     * @param cursor            the cursor containing the data set
     * @param sortedColumnIndex the column number in the cursor that is sorted
     *                          alphabetically
     * @param alphabet          string containing the indexable characters.
     *                          For example, use the string "%ABCDEFGHIJKLMNOPQRSTUVWXYZ#" , the last character
     *                          "#" represents digits, and the first character represents ASCII characters except digits
     *                          and alphabet.
     */
    public ContactsIndexer(Cursor cursor, int sortedColumnIndex, CharSequence alphabet) {
        super(cursor, sortedColumnIndex, alphabet);
        mContactsIndexer = (String) alphabet;
        mAlphabetLength = alphabet.length();
    }


    @Override
    public int getSectionForPosition(int position) {
        int savedCursorPos = mDataCursor.getPosition();
        mDataCursor.moveToPosition(position);
        String sortedKey = mDataCursor.getString(mColumnIndex);
        mDataCursor.moveToPosition(savedCursorPos);

        if (sortedKey.length() != 0) {
            char firstChar = sortedKey.charAt(0);
            if (firstChar >= '0' && firstChar <= '9') {
                return mContactsIndexer.length() - 1;
            }
            if ((firstChar >= 'A' && firstChar <= 'Z') ||
                    (firstChar >= 'a' && firstChar <= 'z')) {
                // Linear search, as there are only a few items in the section index
                // Could speed this up later if it actually gets used.
                for (int i = 1; i < mAlphabetLength - 1; i++) {
                    char letter = mContactsIndexer.charAt(i);
                    String targetLetter = Character.toString(letter);
                    if (compare(Character.toString(firstChar), targetLetter) == 0) {
                        return i;
                    }
                }
            }
        }
        return 0;
    }

    public String getContactsIndexer() {
        return mContactsIndexer;
    }
}


