package com.ckt.recyclerviewindexerexample;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import java.io.FileNotFoundException;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> implements SectionIndexer {
    private Context mContext;
    private Cursor mCursor;
    private ContactsIndexer mContactsIndexer;
    private onContactClickListener mListener;

    ContactsAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
        mContactsIndexer = new ContactsIndexer(cursor, ContactsQueryInterface.INDEX_NAME);
    }

    @Override
    public ContactsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.contacts_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ContactsAdapter.ViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        // set contact's name
        holder.mContactTextView.setText(mCursor.getString(ContactsQueryInterface.INDEX_NAME));
        // load thumbnail photo
        String thumbnailUri = mCursor.getString(ContactsQueryInterface.INDEX_PHOTO);
        if (thumbnailUri != null) {
            Uri uri = Uri.parse(thumbnailUri);
            try {
                AssetFileDescriptor afd = mContext.getContentResolver().openAssetFileDescriptor(uri, "r");
                if (afd != null)
                    holder.mAvator.setImageBitmap(BitmapFactory.decodeFileDescriptor(afd.getFileDescriptor()));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            holder.mAvator.setImageResource(R.mipmap.ic_launcher_round);
        }

        // set item click listener
        final Uri contactUri = ContactsContract.Contacts.getLookupUri(
                mCursor.getLong(ContactsQueryInterface.INDEX_ID),
                mCursor.getString(ContactsQueryInterface.INDEX_LOOKUP_KEY)
        );
        holder.itemView.setOnClickListener(v -> mListener.onContactClick(contactUri));
    }

    @Override
    public int getItemCount() {
        return mCursor != null ? mCursor.getCount() : 0;
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
        TextView mContactTextView;
        ImageView mAvator;

        ViewHolder(View itemView) {
            super(itemView);
            mContactTextView = itemView.findViewById(android.R.id.text1);
            mAvator = itemView.findViewById(android.R.id.icon);
        }
    }

    void swapCursor(Cursor c) {
        mCursor = c;
        mContactsIndexer.setCursor(mCursor);
        notifyDataSetChanged();
    }

    public interface onContactClickListener {
        void onContactClick(Uri uri);
    }

    public void setOnContactClickListener(onContactClickListener listener) {
        mListener = listener;
    }
}
