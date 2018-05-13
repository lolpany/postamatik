package lol.lolpany.postamatik;

import lol.lolpany.Account;
import lol.lolpany.ComponentConnection;
import lol.lolpany.Location;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Set;

public class ContentRepository {

    private final ComponentConnection<ContentRepositoryStore> contentRepositoryStoreQueue;
    private ContentRepositoryStore contentRepositoryStore;
    private final PostsTimeline postsTimeline;

    public ContentRepository(ComponentConnection<ContentRepositoryStore> contentRepositoryStoreQueue,
                             PostsTimeline postsTimeline) {
        this.contentRepositoryStoreQueue = contentRepositoryStoreQueue;
        this.postsTimeline = postsTimeline;
    }

//    void addContentSearch(ContentSearch contentSearch) {
//        contentSearchList.add(contentSearch);
//    }
//
//    void addContent(Content content) {
//        contentList.add(content);
//    }

    Content getContent(double precision, Set<String> tags, Account account, Location location, PostsTimeline timeline)
            throws InterruptedException, IOException, GeneralSecurityException {


        ContentRepositoryStore newContentRepositoryStore = contentRepositoryStoreQueue.poll();
        if (newContentRepositoryStore != null) {
            contentRepositoryStore = newContentRepositoryStore;
        }

        if (contentRepositoryStore != null) {
            for (Content content : contentRepositoryStore.contentList) {
                if (!timeline.isAlreadyScheduledOrUploadedOrPosted(location.url.toString(), content)
                        && Utils.match(tags, content.tags) >= precision) {
                    return content;
                }
            }
            Collections.shuffle(contentRepositoryStore.contentSearchList);
            for (ContentSearch contentSearch : contentRepositoryStore.contentSearchList) {
                Content content = contentSearch.findContent(precision, tags, postsTimeline, account, location);
                if (content != null) {
//                    addContent(content);
                    if (!timeline.isAlreadyScheduledOrUploadedOrPosted(location.url.toString(), content)
                            && Utils.match(tags, content.tags) >= precision) {
                        return content;
                    }
                }
            }
        }
        return null;
    }
}
