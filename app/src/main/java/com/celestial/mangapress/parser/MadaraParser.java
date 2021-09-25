package com.celestial.mangapress.parser;

import com.celestial.mangapress.Database;
import com.celestial.mangapress.dao.ChapterEntityDao;
import com.celestial.mangapress.entities.ChapterEntity;
import com.celestial.mangapress.entities.MangaTileEntity;
import com.celestial.mangapress.models.ChapterLink;
import com.celestial.mangapress.models.HomePage;
import com.celestial.mangapress.models.MangaDetails;
import com.celestial.mangapress.models.MangaTile;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lombok.SneakyThrows;

public class MadaraParser {
    private static final String madaraTemplateUrl = "https://manganato.com/genre-all/";
    private static final String searchEndpoint = "https://manganato.com/search/story/";

    public Document mainDoc;

    public MadaraParser() throws IOException {
        init();
    }

    public void init() throws IOException {
        this.mainDoc = Jsoup.connect(madaraTemplateUrl).get();

    }

    @SneakyThrows
    public List<MangaTile> getPage(int page) {
        Document nextPage = Jsoup.connect(madaraTemplateUrl + page).get();
        return parsePageListingItems(nextPage);
    }

    public HomePage getHomePageDetails() {
        List<MangaTile> popularMangas = getPopularTiles();
        List<MangaTile> latestUpdates = getLatestReleases();
        return new HomePage(popularMangas, latestUpdates);
    }

    public List<MangaTile> getLatestReleases() {
        return parsePageListingItems(mainDoc);
    }

    public List<MangaTile> parsePageListingItems(Document document) {
        List<MangaTile> mangaTiles = new ArrayList<MangaTile>();
        Elements latestReleases = document.select(".content-genres-item");
        for (Element element : latestReleases) {
            MangaTile mangaTile = new MangaTile();
            mangaTile.setTitle(element.select(".genres-item-info > h3").text());
            mangaTile.setUrl(element.select(".genres-item-info > h3 > a").attr("href"));
            mangaTile.setImage(element.select(".genres-item-img > img").attr("src"));
            mangaTiles.add(mangaTile);
        }
        return mangaTiles;
    }

    /**
     * @param element
     * @return
     * @deprecated
     */
    public List<ChapterLink> getLatestReleaseChaptersFromHomePage(Element element) {
        List<ChapterLink> chapterLinks = new ArrayList<ChapterLink>();
        Elements elements = element.select(".item-summary > .list-chapter > .chapter-item > .chapter > a");
        listChapterParser(chapterLinks, elements);
        return chapterLinks;
    }

    public void listChapterParser(List<ChapterLink> chapterLinks, Elements elements) {
        for (Element link : elements) {
            chapterLinks.add(new ChapterLink(link.attr("href"), link.text()));
        }
    }

    public static List<MangaTile> getPopularTiles() {
        List<MangaTile> mangaTiles = new ArrayList<MangaTile>();

        List<MangaTileEntity> mangaTileEntities = Database.getMangaTileEntityDao().getAll();

        for (MangaTileEntity mangaTileEntity : mangaTileEntities) {
            MangaTile mangaTile = new MangaTile();
            mangaTile.setImage(mangaTileEntity.getImage());
            mangaTile.setTitle(mangaTileEntity.getTitle());
            mangaTile.setUrl(mangaTileEntity.getUrl());
            mangaTiles.add(mangaTile);
        }
        return mangaTiles;
    }

    /**
     * @param element
     * @return
     * @deprecated
     */
    public List<ChapterLink> getPopularTilesChaptersFromHomePage(Element element) {
        List<ChapterLink> chapterLinks = new ArrayList<ChapterLink>();
        Elements elements = element.select(".popular-content > .list-chapter > .chapter-item > .chapter > a");
        listChapterParser(chapterLinks, elements);
        return chapterLinks;
    }

    @SneakyThrows
    public static MangaDetails getMangaDetailsFromMangaTile(MangaTile mangaTile) {
        MangaDetails mangaDetails = new MangaDetails();
        Document mangaDetailsDoc = Jsoup.connect(mangaTile.getUrl()).get();
        mangaDetails.setTitle(mangaTile.getTitle());
        mangaDetails.setUrl(mangaTile.getUrl());
        mangaDetails.setImage(
                mangaDetailsDoc
                        .select(".info-image")
                        .get(0)
                        .select("img")
                        .get(0)
                        .attr("src")
        );
        mangaDetails.setDescription(
                mangaDetailsDoc
                        .select(".panel-story-info-description").text()
        );
        List<ChapterLink> chapterLinks = getChapterLinkForMangaDetails(mangaDetailsDoc);
        mangaDetails.setChapterLinks(chapterLinks);
        return mangaDetails;
    }

    private static List<ChapterLink> getChapterLinkForMangaDetails(Document document) {
        Elements elements = document.select("li.a-h");
        List<ChapterLink> chapterLinks = new ArrayList<>();
        for (Element element : elements) {
            Element chapterLinkHtml = element.select("a").get(0);
            String chapterUrl = chapterLinkHtml.attr("href");
            ChapterLink chapterLink = new ChapterLink(chapterUrl, chapterLinkHtml.text());
            chapterLinks.add(chapterLink);
        }
        return chapterLinks;
    }

    public static void updateChapterLinkWithDatabaseStatus(MangaTile mangaTile, List<ChapterLink> chapterLinks) {
        ChapterEntityDao chapterEntityDao = Database.getChapterEntityDao();
        List<ChapterEntity> chapterEntities = chapterEntityDao.getByMangaUrl(mangaTile.getUrl());
        List<String> viewedChapters = new ArrayList<>();
        for (ChapterEntity chapterEntity : chapterEntities) {
            viewedChapters.add(chapterEntity.getChapterUrl());
        }
        for (ChapterLink chapterLink : chapterLinks) {
            chapterLink.setViewed(viewedChapters.contains(chapterLink.getUrl()));
        }
    }

    public static List<String> getImagesFromChapterLink(Document document) {
        Elements images = document.select(".container-chapter-reader > img");
        List<String> imageUrl = new ArrayList<>();
        for (Element element : images) {
            imageUrl.add(element.attr("src").trim());
        }
        return imageUrl;
    }

    public static String nextPageFromViewer(Document document) {
        try {
            Elements next = document.select(".navi-change-chapter-btn > a");
            if (next.last().text().equals("PREV CHAPTER")) {
                return null;
            }
            return next.last().attr("href");
        } catch (Exception e) {
            return null;
        }
    }

    public static String currentChapterName(Document document) {
        String chapterName = "";
        try {
            chapterName = document
                    .select(".panel-breadcrumb")
                    .get(0)
                    .select("a.a-h").last().text();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return chapterName;
    }

    @SneakyThrows
    public static List<MangaTile> searchManga(String keyword) {
        String sanitizedString = keyword.replaceAll("\\s", "_");
        Document document = Jsoup.connect(searchEndpoint + sanitizedString).get();
        List<MangaTile> mangaTiles = new ArrayList<MangaTile>();
        Elements latestReleases = document.select(".search-story-item");
        for (Element element : latestReleases) {
            MangaTile mangaTile = new MangaTile();
            mangaTile.setTitle(element.select(".item-right > h3").text());
            mangaTile.setUrl(element.select(".item-right > h3 > a").attr("href"));
            mangaTile.setImage(element.select(".item-img > img").attr("src"));
            mangaTiles.add(mangaTile);
        }
        return mangaTiles;
    }

}
