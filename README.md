# Screenshot
![indexer](https://github.com/buxiliulian/RecyclerViewIndexerExample/blob/master/screenshots/indexer.gif)
![alphabet_indexer](https://github.com/buxiliulian/RecyclerViewIndexerExample/blob/master/screenshots/alphabet_indexer.gif)

# RecyclerViewIndexer library
RecyclerViewIndexer Library use RecyclerView.ItemDecoration to draw a indexer, but not override RecyclerView
or custom a view to implement indexer.

# How to use
## import
```
compile 'com.buxiliulian.rv:recyclerviewindexer:0.1.1'
```
## usage

### add decoration
```java
        mContactList = findViewById(R.id.contacts);

        // add sticky head with divider
        mContactsList.addItemDecoration(new StickyHeaderDecoration(this, StickyHeaderDecoration.VERTICAL));
        // add indexer
        IndexerDecoration indexerDecoration = new IndexerDecoration.Builder(this,
                (rv, sectionIndex) -> {
                    // Fast scroll to specified position
                    RecyclerView.Adapter adapter = rv.getAdapter();
                    if (adapter instanceof SectionIndexer) {
                        SectionIndexer indexer = (SectionIndexer) adapter;
                        int pos = indexer.getPositionForSection(sectionIndex);
                        RecyclerView.LayoutManager layoutManager = rv.getLayoutManager();
                        if (layoutManager instanceof LinearLayoutManager) {
                            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
                            linearLayoutManager.scrollToPositionWithOffset(pos, 0);
                        }
                    }
                })
                .sectionTextSize(12)
                .horizontalPadding(IndexerDecoration.DEFAULT_HORIZONTAL_PADDING)
                .balloonColor(IndexerDecoration.DEFAULT_BALLOON_COLOR)
                .build();
        mContactsList.addItemDecoration(indexerDecoration);
```


