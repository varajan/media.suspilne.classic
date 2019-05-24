package media.suspilne.classic;

public class TrackEntry{
    public int id;
    public int titleId;
    public int authorNameId;
    public int authorPhotoId;

    public TrackEntry(){ id = -1; }

    public TrackEntry(int id, int title, int name, int photo){
        this.id = id;
        this.titleId = title;
        this.authorNameId = name;
        this.authorPhotoId = photo;
    }

    public String stream(){
        return stream(id);
    }

    public String stream(int track){
        return String.format("https://classical.suspilne.media/inc/audio/%02d.mp3", track);
    }
}