package com.ckt.recyclerviewindexerexample;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SectionIndexer;

import com.ckt.recyclerviewindexer.ContactsIndexer;
import com.ckt.recyclerviewindexer.IndexerDecoration;
import com.ckt.recyclerviewindexer.StickyHeaderDecoration;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private RecyclerView mContactsList;
    private ContactsAdapter mAdapter;

    private static final String PERMISSION_READ_CONTACT = Manifest.permission.READ_CONTACTS;
    private static final String PERMISSION_WRITE_CONTACT = Manifest.permission.WRITE_CONTACTS;
    private static final String[] PERMISSIONS_CONTACT = {PERMISSION_READ_CONTACT, PERMISSION_WRITE_CONTACT};

    // request code for contact permission
    private static final int REQUEST_CONTACT_CODE = 0x001;

    // contact loader id
    private static final int CONTACT_LOADER_ID = 0x000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //  need to request contact permission before create contact loader
        if (ActivityCompat.checkSelfPermission(this, PERMISSION_READ_CONTACT) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, PERMISSION_WRITE_CONTACT) != PackageManager.PERMISSION_GRANTED) {
            requestContactPermission();
        } else {
            getSupportLoaderManager().initLoader(CONTACT_LOADER_ID, null, this);
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
        });

        // init RecyclerView
        mContactsList = findViewById(R.id.contacts);
        mContactsList.setLayoutManager(new LinearLayoutManager(this));
        mContactsList.setAdapter(mAdapter = new ContactsAdapter(this, null));
        mAdapter.setOnContactClickListener(uri -> {
        });
        mContactsList.addItemDecoration(new StickyHeaderDecoration(this, StickyHeaderDecoration.VERTICAL));
        IndexerDecoration indexerDecoration = new IndexerDecoration.Builder(this, ContactsIndexer.DEFAULT_INDEXER_CHARACTERS)
                .indexerTextSize(12)
                .horizontalPadding(IndexerDecoration.DEFAULT_OUTLINE_HORIZONTAL_PADDING_DP)
                .balloonColor(IndexerDecoration.DEFAULT_BALLOON_BG_COLOR)
                .build();
        indexerDecoration.attachToRecyclerView(mContactsList, (rv, sectionIndex) -> {
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

    private void requestContactPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, PERMISSION_READ_CONTACT)) {
            Snackbar.make(mContactsList, R.string.contact_permission, Snackbar.LENGTH_INDEFINITE)
                    .setAction("OK", v ->
                            ActivityCompat.requestPermissions(this, PERMISSIONS_CONTACT, REQUEST_CONTACT_CODE)
                    ).show();
        } else {
            ActivityCompat.requestPermissions(this, PERMISSIONS_CONTACT, REQUEST_CONTACT_CODE);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        return new CursorLoader(
                this,
                ContactsContract.Contacts.CONTENT_URI,
                ContactsQueryInterface.PROJECTION,
                null,
                null,
                ContactsContract.Contacts.SORT_KEY_PRIMARY);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == CONTACT_LOADER_ID) {
            mAdapter.swapCursor(data);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        if (loader.getId() == CONTACT_LOADER_ID) {
            mAdapter.swapCursor(null);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CONTACT_CODE) {
            getSupportLoaderManager().initLoader(CONTACT_LOADER_ID, null, this);
        }
    }
}
