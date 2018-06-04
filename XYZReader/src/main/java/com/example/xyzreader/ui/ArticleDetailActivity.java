package com.example.xyzreader.ui;


import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;
import com.squareup.picasso.Picasso;

/**
 * An activity representing a single Article detail screen, letting you swipe between articles.
 */
public class ArticleDetailActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String KEY_IMAGE_URL = "image_url";

    private Cursor mCursor;
    private long mStartId;

    private long mSelectedItemId;

    private ViewPager mPager;
    private MyPagerAdapter mPagerAdapter;
    private ImageView backDropIV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_article_detail);

        getSupportLoaderManager().initLoader(0, null, this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        backDropIV = findViewById(R.id.iv_thumbnail);

        mPagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        mPager = findViewById(R.id.pager);
        mPager.setAdapter(mPagerAdapter);
        mPager.setPageMargin((int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
        mPager.setPageMarginDrawable(new ColorDrawable(0x22000000));

        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (mCursor != null) {
                    mCursor.moveToPosition(position);
                    String imageUrl = mCursor.getString(ArticleLoader.Query.PHOTO_URL);
                    showBackdropImage(imageUrl);
                }
                mSelectedItemId = mCursor.getLong(ArticleLoader.Query._ID);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        FloatingActionButton fab = findViewById(R.id.share_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mimeType = "text/plain";
                String title = getString(R.string.action_share);

                Intent shareIntent = ShareCompat.IntentBuilder.from(ArticleDetailActivity.this)
                        .setChooserTitle(title)
                        .setType(mimeType)
                        .setText("Some sample text")
                        .getIntent();
                if (shareIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(shareIntent);
                }
            }
        });

        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().getData() != null) {
                mStartId = ItemsContract.Items.getItemId(getIntent().getData());
                mSelectedItemId = mStartId;
            }
        } else {
            String imageUrl = savedInstanceState.getString(KEY_IMAGE_URL);
            showBackdropImage(imageUrl);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mCursor = cursor;
        mPagerAdapter.notifyDataSetChanged();

        // Select the start ID
        if (mStartId > 0) {
            mCursor.moveToFirst();
            // TODO: optimize
            while (!mCursor.isAfterLast()) {
                if (mCursor.getLong(ArticleLoader.Query._ID) == mStartId) {
                    final int position = mCursor.getPosition();
                    mPager.setCurrentItem(position, false);
                    break;
                }
                mCursor.moveToNext();
            }
            mStartId = 0;

            String imageUrl = mCursor.getString(ArticleLoader.Query.PHOTO_URL);
            showBackdropImage(imageUrl);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        mPagerAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_IMAGE_URL, mCursor.getString(ArticleLoader.Query.PHOTO_URL));
    }

    private void showBackdropImage(String imageUrl) {
        Picasso.get()
                .load(imageUrl)
                .into(backDropIV);
    }

    private class MyPagerAdapter extends FragmentStatePagerAdapter {
        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            mCursor.moveToPosition(position);
            String title = mCursor.getString(ArticleLoader.Query.TITLE);
            String author = mCursor.getString(ArticleLoader.Query.AUTHOR);
            String date = mCursor.getString(ArticleLoader.Query.PUBLISHED_DATE);
            String text = mCursor.getString(ArticleLoader.Query.BODY);
            return ArticleDetailFragment.newInstance(title, author, date, text);
        }

        @Override
        public int getCount() {
            return (mCursor == null) ? 0 : mCursor.getCount();
        }
    }
}
