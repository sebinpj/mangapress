package com.celestial.mangapress;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.celestial.mangapress.adapters.ChapterLinksAdapter;
import com.celestial.mangapress.dao.MangaTileEntityDao;
import com.celestial.mangapress.entities.MangaTileEntity;
import com.celestial.mangapress.models.ChapterLink;
import com.celestial.mangapress.models.MangaDetails;
import com.celestial.mangapress.models.MangaTile;
import com.celestial.mangapress.parser.MadaraParser;
import com.flurry.android.FlurryAgent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MangaDetailsPage extends AppCompatActivity implements ChapterLinksAdapter.ChapterClickListener {
    MangaTile mangaTile;
    MangaDetails mangaDetails;
    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;
    boolean sorAscending = false;
    TextView description;
    ChapterLinksAdapter chapterLinksAdapter;
    SwipeRefreshLayout swipeRefreshLayout;
    Button saveButton;
    private static final String SAVE = "SAVE";
    private static final String DELETE = "Delete";
    MangaTileEntityDao mangaTileEntityDao = Database.getMangaTileEntityDao();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manga_details_page);
        description = findViewById(R.id.manga_details_description);
        swipeRefreshLayout = findViewById(R.id.details_swiper);
        if (getIntent().hasExtra("manga")) {
            mangaTile = getIntent().getParcelableExtra("manga");
            new SetMangaDetails().execute();
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    new SetMangaDetails().execute();
                }
            });
            saveButton = findViewById(R.id.save_manga);
            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveManga();
                }
            });
            checkSavedManga();
        }
    }

    public void checkSavedManga() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                FlurryAgent.logEvent("saved_manga");
                String saveButtonText = saveButton.getText().toString();
                MangaTileEntity mangaTileEntity = new MangaTileEntity();
                mangaTileEntity.setImage(mangaTile.getImage());
                mangaTileEntity.setTitle(mangaTile.getTitle());
                mangaTileEntity.setUrl(mangaTile.getUrl());
                MangaTileEntity found = mangaTileEntityDao.getByUrl(mangaTile.getUrl());
                String saveButtonTextV = SAVE;
                if (found != null) {
                    changeSaveButton(DELETE);
                }
            }
        });
        t.start();
    }

    public void changeSaveButton(String buttonText) {
        runOnUiThread(() -> saveButton.setText(buttonText));
    }

    public void saveManga() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                FlurryAgent.logEvent("saved_manga");
                String saveButtonText = saveButton.getText().toString();
                MangaTileEntity mangaTileEntity = new MangaTileEntity();
                mangaTileEntity.setImage(mangaTile.getImage());
                mangaTileEntity.setTitle(mangaTile.getTitle());
                mangaTileEntity.setUrl(mangaTile.getUrl());
                if (saveButtonText.equals(DELETE)) {
                    mangaTileEntityDao.delete(mangaTileEntity);
                    changeSaveButton(SAVE);
                } else {
                    mangaTileEntityDao.insertAll(mangaTileEntity);
                    changeSaveButton(DELETE);
                }
            }
        });
        t.start();
    }

    public void refreshingStatus(boolean refresh) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(refresh);
            }
        });
    }

    class SetMangaDetails extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                setTitle("Loading...");
                mangaDetails = MadaraParser.getMangaDetailsFromMangaTile(mangaTile);
            } catch (Exception e) {
                flurryLog("manga_details_load_failure");
                e.printStackTrace();
            }
            refreshingStatus(true);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            flurryLog("manga_details_load_success");
            fetchChapters();
            refreshingStatus(false);
        }
    }

    private void flurryLog(String event) {
        Map<String, String> mangaTileMap = new HashMap<String, String>();
        mangaTileMap.put("manga", mangaTile.getTitle());
        mangaTileMap.put("manga_url", mangaTile.getUrl());
        FlurryAgent.logEvent(event, mangaTileMap);
    }

    public void fetchChapters() {
        if (mangaDetails != null) {
            setTitle(mangaTile.getTitle());
            TextView title = findViewById(R.id.manga_details_title);
            title.setText(mangaTile.getTitle());
            ImageView imageView = findViewById(R.id.manga_details_img);
            Glide.with(this).load(mangaDetails.getImage()).into(imageView);

            description.setText(mangaDetails.getDescription());
            description.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (description.getMaxLines() <= 4) {
                        description.setMaxLines(100);
                        FlurryAgent.logEvent("description_expanded");
                    } else {
                        description.setMaxLines(4);
                        FlurryAgent.logEvent("description_collapsed");
                    }
                }
            });

            recyclerView = findViewById(R.id.manga_details_chapters);
            linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, sorAscending);
            linearLayoutManager.setStackFromEnd(sorAscending);
            chapterLinksAdapter = new ChapterLinksAdapter(this, mangaDetails.getChapterLinks(), this);
            new AsyncCheckChapterStatus().execute();
            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.setAdapter(chapterLinksAdapter);
            Button sort = findViewById(R.id.sort_chapters);
            sort.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setSorAscending();
                    Map<String, String> mangaTileMap = new HashMap<String, String>();
                    mangaTileMap.put("manga", mangaTile.getTitle());
                    mangaTileMap.put("manga_url", mangaTile.getUrl());
                    mangaTileMap.put("sort_ascending", Boolean.toString(sorAscending));
                    FlurryAgent.logEvent("sort_button_clicked", mangaTileMap);
                }
            });
        } else {
            setTitle("Fetch failed try again");
            description.setText("retry this operation! by swiping down from the top");
        }
    }

    public void setSorAscending() {
        sorAscending = !sorAscending;
        linearLayoutManager.setReverseLayout(sorAscending);
        linearLayoutManager.setStackFromEnd(sorAscending);
    }

    @Override
    public void onClick(ChapterLink chapterLink) {
        System.out.println(chapterLink);
        Intent intent = new Intent(this, ViewChapter.class);
        intent.putExtra("chapter_link", chapterLink);
        intent.putExtra("manga", mangaTile);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        new AsyncCheckChapterStatus().execute();
    }

    class AsyncCheckChapterStatus extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            if (mangaDetails != null && chapterLinksAdapter != null) {
                List<ChapterLink> chapterLinks = mangaDetails.getChapterLinks();
                MadaraParser.updateChapterLinkWithDatabaseStatus(mangaTile, chapterLinks);
                updateChapter(chapterLinks);
            }
            return null;
        }
    }

    private void updateChapter(List<ChapterLink> chapterLinks) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                chapterLinksAdapter.updateChapterLinks(chapterLinks);
            }
        });
    }

}