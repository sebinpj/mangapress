package com.celestial.mangapress;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.celestial.mangapress.adapters.MangaTilesAdapter;
import com.celestial.mangapress.models.HomePage;
import com.celestial.mangapress.models.MangaTile;
import com.celestial.mangapress.parser.MadaraParser;
import com.celestial.mangapress.view.OneTimeAlertDialog;
import com.flurry.android.FlurryAgent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.SneakyThrows;

public class MainActivity extends AppCompatActivity implements MangaTilesAdapter.OnClickListener {
    MadaraParser madaraParser;
    RecyclerView popularRecyclerView;
    RecyclerView recentRecyclerView;
    RecyclerView searchRecyclerView;
    TextView popularText;
    TextView recentText;
    MangaTilesAdapter mangaTilesAdapter;
    MangaTilesAdapter searchAdapter;
    MangaTilesAdapter popularTilesAdapter;
    List<MangaTile> popularTiles;
    List<MangaTile> recentTiles;
    boolean finisedAll = false;
    boolean loading = false;
    int page = 1;
    int gridSpanCount = 4;
    SearchView searchView;

    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Database.initialize(this);
        searchRecyclerView = findViewById(R.id.searchResults);
        searchAdapter = new MangaTilesAdapter(this, null, this);
        searchRecyclerView.setAdapter(searchAdapter);
        searchRecyclerView.setLayoutManager(new GridLayoutManager(this, gridSpanCount));
        popularText = findViewById(R.id.popular_item);
        recentText = findViewById(R.id.latestReleases);
        swipeRefreshLayout = findViewById(R.id.main_swiper);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new InitializeMangas().execute();
            }
        });

        new InitializeMangas().execute();
        new OneTimeAlertDialog.Builder(this, "alpha_dialog")
                .setTitle("This app is in ALPHA")
                .setMessage(
                        "\nAll contents in this app is fetched from external sources and are not affiliated with the developer." +
                                "\n\nThis app is heavily dependent on network so unstable INTERNET may cause the app to crash ." +
                                "\n\nSome images maybe inverted in color due to a known bug" +
                                "\n\nTHIS APP IS IN ALPHA AND HAS MANY KNOWN AND UNKNOWN BUGS"
                                + "\n\nSome of these will be addressed in a future update.")
                .show();
    }

    public void refreshingStatus(boolean refresh) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(refresh);
            }
        });
    }

    class InitializeMangas extends AsyncTask<Void, Void, Void> {

        @SneakyThrows
        @Override
        protected Void doInBackground(Void... voids) {
            setTitle("Loading...");
            madaraParser = new MadaraParser();
            refreshingStatus(true);
            return null;
        }

        protected void onPostExecute(Void feed) {
            initView();
            setTitle("Home");
            refreshingStatus(false);
        }
    }


    @SneakyThrows
    public void initView() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                HomePage homePageDetails = madaraParser.getHomePageDetails();
                popularTiles = homePageDetails.getSavedMangas();
                recentTiles = homePageDetails.getLatestReleases();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setPopularItems();
                        setLatestUpdates();
                    }
                });
            }
        });
        t.start();
    }

    class Paginator extends AsyncTask<Void, Void, List<MangaTile>> {

        @Override
        protected List<MangaTile> doInBackground(Void... voids) {
            loading = true;
            return madaraParser.getPage(page);
        }

        @Override
        protected void onPostExecute(List<MangaTile> nextSetOfTiles) {
            if (nextSetOfTiles.size() == 0) {
                finisedAll = true;
                FlurryAgent.logEvent("reached_last");
            } else {
                Map<String, String> paginateMap = new HashMap<String, String>();
                paginateMap.put("page", Integer.toString(page));
                FlurryAgent.logEvent("paginated", paginateMap);
                mangaTilesAdapter.addMangaTiles(nextSetOfTiles);
                page++;
            }
            loading = false;
        }
    }

    public void setPopularItems() {
        popularRecyclerView = findViewById(R.id.popular_mangalist);
        popularTilesAdapter = new MangaTilesAdapter(this, popularTiles, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, true);
        layoutManager.setStackFromEnd(true);
        popularRecyclerView.setLayoutManager(layoutManager);
        popularRecyclerView.setAdapter(popularTilesAdapter);
        popularRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                swipeRefreshLayout.setEnabled(newState == RecyclerView.SCROLL_STATE_IDLE);
            }
        });
    }

    public void setLatestUpdates() {
        recentRecyclerView = findViewById(R.id.latest_mangalist);
        GridLayoutManager layoutManager = new GridLayoutManager(this, gridSpanCount);
        mangaTilesAdapter = new MangaTilesAdapter(this, recentTiles, this);
        recentRecyclerView.setLayoutManager(layoutManager);
        recentRecyclerView.setAdapter(mangaTilesAdapter);
        recentRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                if ((visibleItemCount + firstVisibleItemPosition + 16) >= totalItemCount
                        && firstVisibleItemPosition >= 0
                        && totalItemCount >= recentTiles.size() && !finisedAll && !loading) {
                    new Paginator().execute();
                }
            }

        });
    }


    @Override
    public void onClick(MangaTile mangaTile) {
        System.out.println(mangaTile.toString());
        Intent intent = new Intent(this, MangaDetailsPage.class);
        intent.putExtra("manga", mangaTile);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                List<MangaTile> mangaTiles = MadaraParser.getPopularTiles();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (popularTilesAdapter != null && mangaTiles != null & mangaTiles.size() > 0) {
                            popularTilesAdapter.setMangaTiles(mangaTiles);
                        }
                    }
                });
            }
        });
        t.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.manga_search, menu);

        MenuItem menuSearchItem = menu.findItem(R.id.manga_search_icon);

        searchView = (SearchView) menuSearchItem.getActionView();
        searchView.setFocusable(false);
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                toggleHomePage(true);
                return false;
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                searchAdapter.setMangaTiles(null);
                new AsyncSearch().execute(s);
                toggleHomePage(false);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return true;
    }

    @Override
    public void onBackPressed() {
        if (!searchView.isIconified()) {
            searchView.clearFocus();
            searchView.setIconified(true);
            toggleHomePage(true);
        } else {
            super.onBackPressed();
        }
    }

    public void toggleHomePage(boolean visible) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int forHome = visible ? View.VISIBLE : View.GONE;
                int forSearch = visible ? View.GONE : View.VISIBLE;
                popularRecyclerView.setVisibility(forHome);
                recentRecyclerView.setVisibility(forHome);
                popularText.setVisibility(forHome);
                recentText.setVisibility(forHome);
                searchRecyclerView.setVisibility(forSearch);
            }
        });
    }

    class AsyncSearch extends AsyncTask<String, Void, List<MangaTile>> {
        @Override
        protected List<MangaTile> doInBackground(String... strings) {
            refreshingStatus(true);
            FlurryAgent.logEvent("search_start");
            String searchText = strings[0];
            return MadaraParser.searchManga(searchText);
        }

        @Override
        protected void onPostExecute(List<MangaTile> mangaTiles) {
            super.onPostExecute(mangaTiles);
            if (mangaTiles != null && mangaTiles.size() > 0) {
                searchAdapter.setMangaTiles(mangaTiles);
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Search returned empty", Toast.LENGTH_LONG).show();
                    }
                });
            }
            refreshingStatus(false);
        }
    }
}
