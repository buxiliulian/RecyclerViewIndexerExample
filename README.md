# MDContacts
MDContacts is a material design contacts, it use indexer library to build sticky headers with appropriate
dividers and a beautiful indexer aligned to RecyclerView.

# Screenshot
![indexer](https://github.com/buxiliulian/RecyclerViewIndexerExample/blob/master/screenshots/indexer.gif)

# RecyclerViewIndexer library
Indexer Library use RecyclerView.ItemDecoration to draw a indexer, but not override RecyclerView
or custom a view to implement indexer.

# How to use
## import
```
compile 'com.buxiliulian.rv:recyclerviewindexer:0.1.0'
```
## usage
### adapter
Adapter must implement interface SectionIndexer, and use ContactsIndexer extends AlphabetIndexer to implement this interface.
```java
    private class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> implements SectionIndexer {
        private Context mContext;
        private Cursor mCursor;
        private ContactsIndexer mContactsIndexer;

        ContactsAdapter(Context context, Cursor cursor) {
            mContext = context;
            mCursor = cursor;
            mContactsIndexer = new ContactsIndexer(cursor, INDEX_NAME);
        }

        @Override
        public ContactsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // ...
        }

        @Override
        public void onBindViewHolder(ContactsAdapter.ViewHolder holder, int position) {
            // ...
        }

        @Override
        public int getItemCount() {
            // ...
        }

        @Override
        public Object[] getSections() {
            return mContactsIndexer.getSections();
        }

        @Override
        public int getPositionForSection(int sectionIndex) {
            return mContactsIndexer.getPositionForSection(sectionIndex);
        }

        @Override
        public int getSectionForPosition(int position) {
            return mContactsIndexer.getSectionForPosition(position);
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            // ...
        }

        void swapCursor(Cursor c) {
            mCursor = c;
            mContactsIndexer.setCursor(mCursor);
            notifyDataSetChanged();
        }
    }
```

### add decoration
```java
        mContactList = getActivity().findViewById(R.id.contacts);
        mContactList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mContactList.setAdapter(mAdapter = new ContactsAdapter(getActivity(), null));

        // add sticky head with divider
        mContactList.addItemDecoration(new StickyHeaderDecoration(getActivity(), StickyHeaderDecoration.VERTICAL));
        IndexerDecoration indexerDecoration = new IndexerDecoration.Builder(getActivity())
                // character's text size in indexer
                .sectionTextSize(12)
                // indexer padding left and right
                .horizontalPadding(IndexerDecoration.DEFAULT_HORIZONTAL_PADDING)
                // balloon background color
                .balloonColor(IndexerDecoration.DEFAULT_BALLOON_COLOR)
                .build();
        mContactList.addItemDecoration(indexerDecoration);
```

### Configuration
If you indexer is based on alphabet, you can configure indexer to only support it.
```java
            private static final String SECTION_INDEXER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
            // configure IndexerDecoration
            IndexerDecoration indexerDecoration = new IndexerDecoration.Builder(getActivity())
                    // only support alphabet
                    .indexer(SECTION_INDEXER)
                    .sectionTextSize(12)
                    .horizontalPadding(IndexerDecoration.DEFAULT_HORIZONTAL_PADDING)
                    .balloonColor(IndexerDecoration.DEFAULT_BALLOON_COLOR)
                    .build();
            // configure ContactIndexer
            mContactsIndexer = new ContactsIndexer(cursor, INDEX_NAME, SECTION_INDEXER);
```
![alphabet_indexer](https://github.com/buxiliulian/RecyclerViewIndexerExample/blob/master/screenshots/alphabet_indexer.gif)
