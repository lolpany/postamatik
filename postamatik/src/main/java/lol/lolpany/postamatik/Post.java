package lol.lolpany.postamatik;

import java.time.Instant;

public class Post {
    public Instant time;
    public Content content;
    public final Account account;
    public final Location location;
    public PostState postState;
    transient public PostAction action;

    public Post(Instant time, Content content, Account account, Location location) {
        this.time = time;
        this.content = content;
        this.account = account;
        this.location = location;
        this.postState = PostState.SCHEDULED;
    }

    public void setAction(PostAction action) {
        this.action = action;
    }

    public void setUploaded() {
        postState = PostState.UPLOADED;
    }

    public void setPosted() {
        postState = PostState.POSTED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Post post = (Post) o;

        if (time != null ? !time.equals(post.time) : post.time != null) return false;
        if (content != null ? !content.equals(post.content) : post.content != null) return false;
        return location != null ? location.url.equals(post.location.url) : post.location == null;
    }

    @Override
    public int hashCode() {
        int result = time != null ? time.hashCode() : 0;
        result = 31 * result + (content != null ? content.hashCode() : 0);
        result = 31 * result + (account != null ? account.hashCode() : 0);
        result = 31 * result + (location != null ? location.hashCode() : 0);
        result = 31 * result + (action != null ? action.hashCode() : 0);
        return result;
    }
}
