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
  <version>0.1.7</version>
  <type>pom</type>
</dependency>
```

gradle:
```
compile 'com.buxiliulian.rv:recyclerviewindexer:0.1.7'
```

# Usage
```java
        SimpleIndexer.Builder builder = new SimpleIndexer.Builder(this, ContactsIndexer.DEFAULT_INDEXER_CHARACTERS)
                .indexerTextSize(12)
                .padding(SimpleIndexer.DEFAULT_PADDING_DP)
                .indicatorColor(SimpleIndexer.DEFAULT_INDICATOR_BG_COLOR);
        SimpleIndexer balloonIndexer = new BalloonIndexer(builder);

        balloonIndexer.attachToRecyclerView(mContactsList, (rv, sectionIndex) -> {
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
        });
    }
```

# Contact & Help
If you want to know more, please refer to https://blog.csdn.net/zwlove5280/article/details/80979207.

Please feel free to contact me if you have any question when using this library.
1. blog: https://my.csdn.net/zwlove5280
2. QQ Email: 509643792@qq.com
3. Gmail: zwlove5250@gmail.com

