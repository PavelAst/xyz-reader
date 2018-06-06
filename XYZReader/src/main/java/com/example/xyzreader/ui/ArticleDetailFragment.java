package com.example.xyzreader.ui;


import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.xyzreader.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment {

    private static final String TAG = "ArticleDetailFragment";
    private static final int MAX_LENGTH_TEXT = 1000;

    public static final String ARG_TITLE = "item_title";
    public static final String ARG_AUTHOR = "item_author";
    public static final String ARG_DATE = "item_date";
    public static final String ARG_TEXT = "item_text";

    private static final float PARALLAX_FACTOR = 1.25f;

    private View mRootView;
    private int mMutedColor = 0xFF333333;
    private ColorDrawable mStatusBarColorDrawable;

    private String mTitle;
    private String mAuthor;
    private String mDate;
    private String mText;

    private Button mShowMoreButton;

    private boolean mIsCard = false;
    private int mStatusBarFullOpacityBottom;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
    // Use default locale format
    private SimpleDateFormat outputFormat = new SimpleDateFormat();
    // Most time functions can only handle 1902 - 2037
    private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2, 1, 1);

    TextView mTextBodyTV;
    private boolean isFullText = false;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {
    }

    // ArticleDetailFragment.newInstance(imageUrl, title, author, date, text);
    public static ArticleDetailFragment newInstance(String title, String author,
                                                    String date, String text) {
        Bundle arguments = new Bundle();
        arguments.putString(ARG_TITLE, title);
        arguments.putString(ARG_AUTHOR, author);
        arguments.putString(ARG_DATE, date);
        arguments.putString(ARG_TEXT, text);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTitle = getArguments().getString(ARG_TITLE);
        mAuthor = getArguments().getString(ARG_AUTHOR);
        mDate = getArguments().getString(ARG_DATE);
        mText = getArguments().getString(ARG_TEXT);

        mIsCard = getResources().getBoolean(R.bool.detail_is_card);
        mStatusBarFullOpacityBottom = getResources().getDimensionPixelSize(
                R.dimen.detail_card_top_margin);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);

        mShowMoreButton = mRootView.findViewById(R.id.btn_show_more);

        mStatusBarColorDrawable = new ColorDrawable(0);

        bindViews();
        return mRootView;
    }

    private Date parsePublishedDate() {
        try {
            return dateFormat.parse(mDate);
        } catch (ParseException ex) {
            Log.e(TAG, ex.getMessage());
            Log.i(TAG, "passing today's date");
            return new Date();
        }
    }

    private void bindViews() {
        if (mRootView == null) {
            return;
        }
        TextView titleViewTV = mRootView.findViewById(R.id.article_title);
        TextView bylineViewTV = mRootView.findViewById(R.id.article_byline);
        bylineViewTV.setMovementMethod(new LinkMovementMethod());
        mTextBodyTV = mRootView.findViewById(R.id.article_body);

        mTextBodyTV.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "Rosario-Regular.ttf"));

        titleViewTV.setText(mTitle);
        Date publishedDate = parsePublishedDate();
        if (!publishedDate.before(START_OF_EPOCH.getTime())) {
            bylineViewTV.setText(
                    DateUtils.getRelativeTimeSpanString(
                            publishedDate.getTime(),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL).toString()
                            + " by "
                            + mAuthor);

        } else {
            // If date is before 1902, just show the string
            bylineViewTV.setText(outputFormat.format(publishedDate) + " by " + mAuthor);

        }

        if (mText.length() <= MAX_LENGTH_TEXT) {
            mTextBodyTV.setText(fromHtml(mText.replaceAll("(\r\n\r\n|\n\n)", "<br />")));
        } else {
            mTextBodyTV.setText(fromHtml(getPartOfText(mText)));
            mShowMoreButton.setVisibility(View.VISIBLE);
            mShowMoreButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mShowMoreButton.setText(R.string.loading_label);
                    if (!isFullText) {
                        String newString = TextUtils.join("<br />", mText.split("(\r\n\r\n|\n\n)"));
                        mTextBodyTV.setText(fromHtml(newString));
                        mShowMoreButton.setText(R.string.show_less_label);
                        isFullText = true;
                    } else {
                        mTextBodyTV.setText(fromHtml(getPartOfText(mText)));
                        mShowMoreButton.setText(R.string.show_more_label);
                        isFullText = false;
                    }
                }
            });
        }

    }

    @SuppressWarnings("deprecation")
    public Spanned fromHtml(String html) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(html);
        }
    }

    private String getPartOfText(String bigText) {
        int string_num = 12;
        int index = bigText.indexOf("\r\n");

        if (index == -1) {
            return bigText.substring(0, MAX_LENGTH_TEXT) + "...";
        }
        
        int count = 0;
        while (index != -1 && count < string_num) {
            int nextIndex = bigText.indexOf("\r\n", index + 4);
            count += 1;
            index = nextIndex;
        }
        String partOfText = bigText.substring(0, index);
        return partOfText.replaceAll("(\r\n\r\n|\n\n)", "<br />");
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!isVisibleToUser && isFullText) {
            mTextBodyTV.setText(fromHtml(getPartOfText(mText)));
            mShowMoreButton.setText(R.string.show_more_label);
            isFullText = false;
        }
    }

}
