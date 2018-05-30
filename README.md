# RecyclerViewIndexer
RecyclerViewIndexer Library use RecyclerView.ItemDecoration to draw a indexer, but not override RecyclerView
or custom a view to implement indexer.

![alphabet_indexer](https://github.com/buxiliulian/RecyclerViewIndexerExample/blob/master/screenshots/GIF.gif)

## Download
maven:
```
<dependency>
  <groupId>com.buxiliulian.rv</groupId>
  <artifactId>recyclerviewindexer</artifactId>
  <version>0.1.3</version>
  <type>pom</type>
</dependency>
```

gradle:
```
compile 'com.buxiliulian.rv:recyclerviewindexer:0.1.3'
```

# Usage
```java
        IndexerDecoration indexerDecoration = new IndexerDecoration.Builder(this, ContactsIndexer.DEFAULT_INDEXER_CHARACTERS)
                .indexerTextSize(12)
                .horizontalPadding(IndexerDecoration.DEFAULT_OUTLINE_HORIZONTAL_PADDING_DP)
                .balloonColor(IndexerDecoration.DEFAULT_BALLOON_BG_COLOR)
                .build();
        indexerDecoration.attachToRecyclerView(mContactsList, (rv, sectionIndex) -> {
            // do what you want...
        });
```


