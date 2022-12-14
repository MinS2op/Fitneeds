package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import dto.FavoritesDTO;
import dto.GymDTO;


public class FavoritesDAO extends Dao {


    private static FavoritesDAO instance;

    synchronized public static FavoritesDAO getInstance() {
        if (instance == null) {
            instance = new FavoritesDAO();
        }
        return instance;
    }


    /**
     * 즐겨찾기 테이블에 id중복확인
     *
     * @param useq
     * @param gseq
     * @return
     * @throws Exception
     */
    public boolean isFavExist(int useq, int gseq) throws Exception {  //id중복확인 dao
        String sql = "select * from Favorites where user_seq = ? and gym_seq = ?";
        try (Connection con = this.getConnection();
             PreparedStatement pstat = con.prepareStatement(sql)
        ) {
            pstat.setInt(1, useq);
            pstat.setInt(2, gseq);
            try (ResultSet rs = pstat.executeQuery();) {
                boolean result = rs.next(); //존재하면 true
                return result;
                //return rs.next(); 가능
            }
        }
    }


    /**
     * 즐겨찾기 추가
     *
     * @param dto
     * @return
     * @throws Exception
     */
    public int add(FavoritesDTO dto) throws Exception {
        String sql = "insert into favorites values(fav_seq.nextval, ?, ?)";
        try (Connection con = this.getConnection();
             PreparedStatement pstat = con.prepareStatement(sql)) {
            //seq를 직접 넣는 이유는 파일 때문에

            pstat.setInt(1, dto.getUser_seq());
            pstat.setInt(2, dto.getGym_seq());


            int result = pstat.executeUpdate();
            con.commit();
            return result;
        }
    }

    public void addCus(FavoritesDTO favDto) throws Exception {
        String sql = "insert into favorites values(?, ?, ?)";
        try (Connection con = this.getConnection();
             PreparedStatement pstat = con.prepareStatement(sql);) {
            //seq를 직접 넣는 이유는 파일 때문에

            pstat.setInt(1, favDto.getFav_seq());
            pstat.setInt(2, favDto.getUser_seq());
            pstat.setInt(3, favDto.getGym_seq());

            pstat.executeUpdate();
            con.commit();
        }
    }

    /**
     * 즐겨찾기 삭제
     *
     * @param gym_seq gym_seq, user_Seq 기준으로 삭제
     * @return
     * @throws Exception
     */
    public int removeByGymSeq(int gym_seq, int user_seq) throws Exception {  // 즐찾 삭제
        String sql = "delete from favorites where gym_seq = ? and user_seq = ?";
        try (Connection con = this.getConnection();
             PreparedStatement pstat = con.prepareStatement(sql);) {
            pstat.setInt(1, gym_seq);
            pstat.setInt(2, user_seq);
            int result = pstat.executeUpdate();
            con.commit();
            return result;
        }
    }


    /**
     * user sequence가 일치하는 모든 favorite을 list로 불러옴.
     *
     * @param userSeq
     * @return
     * @throws Exception
     */
    public List<Integer> getGymListByUser(int userSeq) throws Exception {
        String sql = "select gym_seq from favorites where user_seq = ? order by fav_seq desc";
        List<Integer> gymsSeq = new ArrayList<>();
        try (
                Connection connection = this.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
        ) {
            statement.setInt(1, userSeq);
            try (
                    ResultSet resultSet = statement.executeQuery();
            ) {
                while (resultSet.next()) {
                    gymsSeq.add(resultSet.getInt("gym_seq"));
                }
                return gymsSeq;
            }
        }
    }

    public int getFavSeqByUserAndGym(int userSeq, int GymSeq) throws Exception {
        String sql = "select fav_seq from favorites where user_seq = ? and gym_seq = ?";
        try (Connection connection = this.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setInt(1, userSeq);
            statement.setInt(2, GymSeq);
            try (ResultSet resultSet = statement.executeQuery();) {

                if (resultSet.next()) {
                    return resultSet.getInt("fav_seq");
                }
                return 0;
            }
        }
    }

    public void deleteByUserSeq(int userSeq) throws Exception {
        String sql = "delete from favorites where user_seq = ?";
        try (Connection connection = this.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
        ) {
            statement.setInt(1, userSeq);

            statement.executeUpdate();
            connection.commit();
        }
    }

    public void deleteByFavoriteSeq(int fav_seq) throws Exception {
        String sql = "delete from favorites where fav_seq = ?";
        try (Connection connection = this.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
        ) {
            statement.setInt(1, fav_seq);

            statement.executeUpdate();
            connection.commit();
        }
    }

    /**
     * gym table을 join 시킨 favorites table을 gym_seq로 그룹화하여 count가 높은 6행 만큼 조회
     *
     * @return
     * @throws Exception
     */
    public List<HashMap<String, Object>> selectSortByFavorites() throws Exception {
        List<HashMap<String, Object>> result = new ArrayList<>();
        String sql = "select * from (select gym_seq, count(*) count, row_number() over(order by count(*) desc) rn from favorites group by gym_seq) f left join gym g on f.gym_seq = g.gym_seq where rn <= 6 order by rn";
        try (Connection con = getConnection();
             PreparedStatement pstat = con.prepareStatement(sql);
             ResultSet rs = pstat.executeQuery();) {

            while (rs.next()) {
                HashMap<String, Object> data = new HashMap<>();
                FavoritesDTO favorites = new FavoritesDTO();
                favorites.setGym_seq(rs.getInt("gym_seq"));
                favorites.setCount(rs.getInt("count"));
                data.put("favorites", favorites);
                data.put("gym", new GymDTO(rs));
                result.add(data);
            }
        }
        return result;
    }


    public void deleteByGymSeq(int gymSeq) throws Exception {
        String sql = "delete from favorites where gym_seq = ?";
        try (Connection con = this.getConnection();
             PreparedStatement statement = con.prepareStatement(sql)) {

            statement.setInt(1, gymSeq);

            statement.executeUpdate();
            con.commit();
        }
    }
}
