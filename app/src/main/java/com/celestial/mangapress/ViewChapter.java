package com.celestial.mangapress;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.celestial.mangapress.adapters.ChapterViewerAdapter;
import com.celestial.mangapress.dao.ChapterEntityDao;
import com.celestial.mangapress.entities.ChapterEntity;
import com.celestial.mangapress.models.ChapterLink;
import com.celestial.mangapress.models.MangaTile;
import com.celestial.mangapress.parser.MadaraParser;
import com.flurry.android.FlurryAgent;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import lombok.SneakyThrows;

public class ViewChapter extends AppCompatActivity {
    ChapterLink chapterLink;
    RecyclerView recyclerView;
    MangaTile mangaTile;
    String nextChapter;
    String currentChapter;
    ChapterViewerAdapter chapterViewerAdapter;
    LinearLayoutManager layoutManager;
    List<String> images = new ArrayList<>();
    boolean loading = false;

    public static void setTimeout(Runnable runnable, int delay) {
        new Thread(() -> {
            try {
                Thread.sleep(delay);
                runnable.run();
            } catch (Exception e) {
                System.err.println(e);
            }
        }).start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_chapter);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setTitle("Loading");
        if (getIntent().hasExtra("chapter_link")) {
            chapterLink = getIntent().getParcelableExtra("chapter_link");
            recyclerView = findViewById(R.id.chapter_images);
        }

        if (getIntent().hasExtra("manga")) {
            mangaTile = getIntent().getParcelableExtra("manga");
            init();
        }
        hideTitleAfter(4000);
    }

    @SneakyThrows
    public void init() {
        layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        chapterViewerAdapter = new ChapterViewerAdapter(this, images);
        recyclerView.setAdapter(chapterViewerAdapter);

        currentChapter = chapterLink.getUrl();

        new LoadChapterImages().execute(chapterLink.getUrl());

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItemCount = ViewChapter.this.layoutManager.getChildCount();
                int totalItemCount = ViewChapter.this.layoutManager.getItemCount();
                int firstVisibleItemPosition = ViewChapter.this.layoutManager.findFirstVisibleItemPosition();
                if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                        && firstVisibleItemPosition >= 0
                        && totalItemCount > 0
                        && nextChapter != null && !loading) {
                    new LoadChapterImages().execute(nextChapter);
                    setReadChapter(currentChapter);
                }
                if (nextChapter == null && !loading) {
                    setReadChapter(currentChapter);
                }
            }
        });
    }

    public void hideTitleAfter(int seconds) {
        setTimeout(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(() -> Objects.requireNonNull(getSupportActionBar()).hide());
            }
        }, seconds);
    }

    public void setReadChapter(String chapterUrl) {
        Thread setReadThread = new Thread(new Runnable() {
            @Override
            public void run() {
                ChapterEntity chapterEntity = new ChapterEntity();
                chapterEntity.setChapterUrl(chapterUrl);
                chapterEntity.setMangaUrl(mangaTile.getUrl());
                ChapterEntityDao chapterEntityDao = Database.getChapterEntityDao();
                chapterEntityDao.insertAll(chapterEntity);
            }
        });
        setReadThread.start();
    }

    class LoadChapterImages extends AsyncTask<String, Document, Document> {

        @SneakyThrows
        @Override
        protected Document doInBackground(String... strings) {
            String url = strings[0];
            loading = true;
            currentChapter = url;
            Document document = null;
            try {
                document = Jsoup.connect(url).get();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return document;
        }

        @Override
        protected void onPostExecute(Document document) {
            if (document != null) {
                runOnUiThread(() -> getSupportActionBar().show());
                String currentChapter = MadaraParser.currentChapterName(document);
                setTitle(currentChapter);
                hideTitleAfter(3000);
                List<String> images = MadaraParser.getImagesFromChapterLink(document);
                nextChapter = MadaraParser.nextPageFromViewer(document);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        viewNext(images);
                    }
                });

                Map<String, String> mangaTileMap = new HashMap<String, String>();
                mangaTileMap.put("manga", mangaTile.getTitle());
                mangaTileMap.put("manga_url", mangaTile.getUrl());
                mangaTileMap.put("chapter_loaded", currentChapter);
                mangaTileMap.put("total_images", Integer.toString(images.size()));
                FlurryAgent.logEvent("chapter_load", mangaTileMap);
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Failed to load chapter", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            loading = false;
        }
    }

    public void viewNext(List<String> images) {
        chapterViewerAdapter.addImages(images);
    }
}