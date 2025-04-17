package interfaces;

/**
 * Interface que define os comportamentos básicos de um objeto Movie
 */
public interface IMovie {
    int getId();
    void setId(int id);
    String getTitle();
    void setTitle(String title);
    String getReleaseDate();
    void setReleaseDate(String releaseDate);
    String getOriginalLanguage();
    void setOriginalLanguage(String originalLanguage);
    double getPopularity();
    void setPopularity(double popularity);
    boolean getAdult();
    void setAdult(boolean adult);
}