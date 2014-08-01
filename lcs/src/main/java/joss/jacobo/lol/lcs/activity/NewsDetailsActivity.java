package joss.jacobo.lol.lcs.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;
import joss.jacobo.lol.lcs.R;
import joss.jacobo.lol.lcs.model.NewsModel;
import joss.jacobo.lol.lcs.utils.Bootstrap;

/**
 * Created by Joss on 7/31/2014
 */
public class NewsDetailsActivity extends BaseActivity {

    public static final String NEWS_MODEL = "news_model";

    @InjectView(R.id.news_details_image)
    ImageView image;
    @InjectView(R.id.news_details_category)
    TextView category;
    @InjectView(R.id.news_details_title)
    TextView title;
    @InjectView(R.id.news_details_author)
    TextView author;
    @InjectView(R.id.news_details_webview)
    WebView webView;
    @InjectView(R.id.news_details_loading)
    LinearLayout loadingView;

    NewsModel newsModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_details);
        ButterKnife.inject(this);

        Gson gson = new Gson();
        newsModel = gson.fromJson(getIntent().getStringExtra(NEWS_MODEL), NewsModel.class);

        setupActionBar();
        onSetActionBarTitle("News", null);

        showLoading();
        setupWebView();
        setContent(newsModel);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        webView.setWebChromeClient(new WebChromeClient());
        webView.getSettings().setJavaScriptEnabled(true);
    }

    public void setContent(NewsModel newsModel) {
        Picasso.with(this).load(newsModel.image).into(image);
        category.setText(newsModel.category.toUpperCase());
        title.setText(Html.fromHtml(newsModel.title));
        author.setText(newsModel.author);

        webView.setWebViewClient(new LcsWebViewClient());
        webView.loadDataWithBaseURL(Bootstrap.BASE_URL, Bootstrap.wrap(newsModel.content), Bootstrap.MIME_TYPE, Bootstrap.ENCODING, null);
    }

    class LcsWebViewClient extends WebViewClient{

        @Override
        public void onPageStarted(WebView wevView, String url, Bitmap favicon) {
            showContent();
        }

        @Override
        public void onPageFinished(WebView webView, String url) {
//            showContent();
        }

    }

    private void showContent() {
        webView.setVisibility(View.VISIBLE);
        loadingView.setVisibility(View.GONE);
    }

    private void showLoading() {
        webView.setVisibility(View.INVISIBLE);
        loadingView.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
