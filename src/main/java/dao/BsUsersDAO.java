package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import commons.Common;
import dto.BsUsersDTO;
import dto.ReviewDTO;
import dto.UserDTO;

public class BsUsersDAO extends Dao {


    private static BsUsersDAO instance;

    synchronized public static BsUsersDAO getInstance() {
        if (instance == null) {
            instance = new BsUsersDAO();
        }
        return instance;
    }

    /**
     * <h1>사업자 회원 날짜 오름차순 정렬 출력</h1>
     *
     * @return
     * @throws Exception
     */
    public List<BsUsersDTO> selectAll() throws Exception {
        List<BsUsersDTO> result = new ArrayList<>();
        String sql = "select * from bs_users order by bs_signup asc";
        try (Connection con = getConnection();
             PreparedStatement pstat = con.prepareStatement(sql);
             ResultSet rs = pstat.executeQuery();) {

            while (rs.next()) {
                result.add(new BsUsersDTO(rs));
            }
            return result;
        }
    }


    /**
     * <h1>관리자 사업자 회원 페이지 회원 검색</h1>
     * 시설갯수 출력과 함께하기위한 쿼리
     * @param name
     * @return
     * @throws Exception
     */

    public List<HashMap<String, Object>> search(String name) throws Exception {
        String sql = "      select * from (select bs_users.*, row_number() over(order by bs_seq desc) rn from bs_users) b left join (select bs_seq, count(*) gym_count from gym group by bs_seq) "
        		+ "g on b.bs_seq = g.bs_seq where gym_count is not null and bs_name like ?";
        try (Connection con = this.getConnection();
             PreparedStatement pstat = con.prepareStatement(sql);) {

        	pstat.setString(1, "%" + name + "%");
          	List<HashMap<String, Object>> list = new ArrayList<>();
            try (ResultSet rs = pstat.executeQuery();) {
             
                
                while (rs.next()) {
                	HashMap<String, Object> data = new HashMap<>();
                
                    data.put("bsuser", new BsUsersDTO(rs));
                    data.put("count", rs.getString("gym_count"));
                    
                    list.add(data);
                }
               return list;
            }
        }
    }

    /**
     * <h1>bs_users 테이블에서 option의 value가 일치하는 모든 컬럼을 조회</h1>
     *
     * @param option
     * @param value
     * @return List<UserDTO>
     * @throws Exception
     */
    public List<BsUsersDTO> searchAll(String option, String value) throws Exception {
        List<BsUsersDTO> result = new ArrayList<>();
        String sql = "select * from bs_users where " + option + " = ?";
        try (Connection con = getConnection();
             PreparedStatement pstat = con.prepareStatement(sql);) {

            pstat.setString(1, value);
            try (ResultSet rs = pstat.executeQuery();) {
                while (rs.next()) {
                    result.add(new BsUsersDTO(rs));
                }
                return result;
            }
        }
    }

    public boolean searchBsPw(String email, String phone) throws Exception {
        String sql = "select bs_email from bs_users where bs_email = ? and bs_phone = ?";
        try (Connection con = getConnection(); PreparedStatement pstat = con.prepareStatement(sql);) {
            pstat.setString(1, email);
            pstat.setString(2, phone);
            try(ResultSet rs = pstat.executeQuery();) {
                return rs.next();
            }
        }
    }

   
    
    /**
     * 삭제기능
     *
     * @param seq
     * @return
     * @throws Exception
     */
    public int deleteBySeq(int seq) throws Exception {  //byseq
        String sql = "delete from bs_users where seq = ?";

        try (Connection con = this.getConnection();
             PreparedStatement pstat = con.prepareStatement(sql);) {

            pstat.setInt(1, seq);
            int result = pstat.executeUpdate();
            con.commit();
            return result;
        }
    }

    /**
     * <h1>사업자 회원가입 창에서 이메일 중복 확인</h1>
     *
     * @param email
     * @return
     * @throws Exception
     */
    public boolean isBsEmailCheck(String email) throws Exception {
        String sql = "select * from bs_users where bs_email = ?";

        try (Connection con = this.getConnection(); PreparedStatement pstat = con.prepareStatement(sql);) {

            pstat.setString(1, email);

            try (ResultSet rs = pstat.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * <h1>사업자 회원가입시 seq 단 한번만 만들어서 시설정보에 사업자 seq 추가하기 위함</h1>
     *
     * @return
     * @throws Exception
     */
    public int getBsSeqNextVal() throws Exception {
        String sql = "select bs_seq.nextval from dual";

        try (Connection con = this.getConnection();
             PreparedStatement pstat = con.prepareStatement(sql);
             ResultSet rs = pstat.executeQuery()) {
            if(rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }

    }

    /**
     * <h1>사업자 회원가입</h1>
     *
     * @param email
     * @param pw
     * @param phone
     * @return
     * @throws Exception
     */
    public int isBsSignUp(BsUsersDTO dto) throws Exception {

        String sql = "insert into bs_users values(?,?,?,?,?,?,sysdate)";

        try (Connection con = this.getConnection(); PreparedStatement pstat = con.prepareStatement(sql);) {

            pstat.setInt(1, dto.getBs_seq());
            pstat.setString(2, dto.getBs_number());
            pstat.setString(3, dto.getBs_email());
            pstat.setString(4, dto.getBs_pw());
            pstat.setString(5, dto.getBs_name());
            pstat.setString(6, dto.getBs_phone());

            int result = pstat.executeUpdate();
            con.commit();

            return result;
        }
    }

    /**
     * 사업자 회원가입시 GYM 필터 추가
     *
     * @param dto
     * @return
     * @throws Exception
     */
    public int isBsSignUp5(BsUsersDTO dto) throws Exception {

        String sql = "insert into gym_filter (gym_seq) values (?)";

        try (Connection con = this.getConnection(); PreparedStatement pstat = con.prepareStatement(sql);) {

            pstat.setInt(1, dto.getBs_seq());
            pstat.setString(2, dto.getBs_number());
            pstat.setString(3, dto.getBs_email());
            pstat.setString(4, dto.getBs_pw());
            pstat.setString(5, dto.getBs_name());
            pstat.setString(6, dto.getBs_phone());

            int result = pstat.executeUpdate();
            con.commit();

            return result;
        }
    }

    
    public List<HashMap<String,Object>> selectByRange(int start, int end) throws Exception { // 한페이지에 출력
        String sql = "select * from (select bs_users.*, row_number() over(order by bs_seq desc) rn from bs_users) b " +
                "left join (select bs_seq, count(*) gym_count from gym group by bs_seq)"
        		+ " g on b.bs_seq = g.bs_seq where rn between ? and ?";
        try (Connection con = this.getConnection(); PreparedStatement pstat = con.prepareStatement(sql);) {

            pstat.setInt(1, start);
            pstat.setInt(2, end);

        	List<HashMap<String, Object>> list = new ArrayList<>();
            try (ResultSet rs = pstat.executeQuery();) {
            
                while (rs.next()) {
                	HashMap<String, Object> data = new HashMap<>();

                    data.put("bsuser", new BsUsersDTO(rs));
                    data.put("count", rs.getString("gym_count"));

                   
                	list.add(data);
                }
                return list;
            }

        }
    }
    
    // 아래로 네비바 로직

    public int getRecordCount() throws Exception { // 게시글 갯수반환
        String sql = "select count(*) from bs_users";

        try (Connection con = this.getConnection();
             PreparedStatement pstat = con.prepareStatement(sql);
             ResultSet rs = pstat.executeQuery()) {
            if(rs.next()) {
                return rs.getInt(1); // 한줄 뽑겠다
            }
            return 0;
        }
    }

    public String getPageNavi(int currentPage, int recordTotalCount) throws Exception {
        int recordCountPerPage = 10;
        int naviCountPerPage = 10;
        int pageTotalCount = 0;
        if (recordTotalCount % recordCountPerPage > 0) {
            pageTotalCount = (recordTotalCount / recordCountPerPage) + 1;
        } else {
            pageTotalCount = (recordTotalCount / recordCountPerPage);
        }
        if (currentPage < 1) {
            currentPage = 1;
        }
        if (currentPage > pageTotalCount) {
            currentPage = pageTotalCount;
        }
        int startNavi = (currentPage - 1) / recordCountPerPage * recordCountPerPage + 1;
        int endNavi = startNavi + naviCountPerPage - 1;
        if (endNavi > pageTotalCount) {
            endNavi = pageTotalCount;
        }
        boolean needPrev = true;
        boolean needNext = true;
        if (startNavi == 1) {
            needPrev = false;
        }
        if (endNavi == pageTotalCount) {
            needNext = false;
        }
        StringBuilder sb = new StringBuilder();
        if (needPrev) {
            sb.append("<li class=\"page-item\"><a class=\"page-link\" href='/bsUserList.host?cpage=" + (startNavi - 1)
                    + "'>Previous</a></li>");
        }
        for (int i = startNavi; i <= endNavi; i++) {
            if (currentPage == i) {
                sb.append("<li class=\"page-item active\" aria-current=\"page\"><a class=\"page-link\" href=\"/bsUserList.host?cpage=" + i + "\">" + i
                        + "</a></li>");
            } else {
                sb.append("<li class=\"page-item\"><a class=\"page-link\" href=\"/bsUserList.host?cpage=" + i + "\">" + i
                        + "</a></li>");
            }
        }
        if (needNext) {
            sb.append("<li class=\"page-item\"><a class=\"page-link\" href='/bsUserList.host?cpage=" + (endNavi + 1)
                    + "'>Next</a></li>");
        }

        return sb.toString();
        /*
         * int pageTotalCount= (recordTotalCount+9) / recordCountPerPage; 수정전
         * recordCountPerPage = 10일때만 해당될겁니다간단하게 페이지당 2페이지보이게하면 저걸로하면 페이지 엄청불어남
         *
         * 수정후 int pageTotalCount= (recordTotalCount+(recordCountPerPage-1)) /
         * recordCountPerPage;
         *
         *
         */
        // 게시글의 갯수 / 한페이지당 보여줄게시글+1=전체페에지 갯수

    }


    /**
     * <h1>BS profile 수정</h1>
     * 수정목록: 3개
     * 이름, 사업자번호, 전화번호
     *
     * @param bsUser
     * @throws Exception
     */
    public void updateProfile(BsUsersDTO bsUser) throws Exception {
        String sql = "update bs_users set bs_number = ? , bs_name = ?, bs_phone = ? where bs_seq = ?";
        try (Connection connection = this.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
        ) {
            statement.setString(1, bsUser.getBs_number());
            statement.setString(2, bsUser.getBs_name());
            statement.setString(3, bsUser.getBs_phone());
            statement.setInt(4, bsUser.getBs_seq());

            statement.executeUpdate();
            connection.commit();
        }
    }

    /**
     * <h1>bsSeq로 하나의 사업자 데이터를 불러옴</h1>
     *
     * @param bsSeq
     * @return
     * @throws Exception
     */
    public BsUsersDTO getByBsSeq(int bsSeq) throws Exception {
        String sql = "select * from bs_users where bs_seq = ?";
        try (Connection con = this.getConnection();
             PreparedStatement statement = con.prepareStatement(sql)
        ) {
            statement.setInt(1, bsSeq);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return new BsUsersDTO(rs);
                }
                return new BsUsersDTO();
            }
        }
    }


    public void updatePw(int bsSeq, String pw) throws Exception {
        String sql = "update bs_users set bs_pw = ? where bs_seq = ?";
        String password = Common.getSHA512(pw);
        try (Connection connection = this.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
        ) {
            statement.setString(1, password);
            statement.setInt(2, bsSeq);

            statement.executeUpdate();
            connection.commit();
        }
    }

    /**
     * BsSeq에 해당하는 데이터 삭제
     * @param bsSeq
     */
    public void deleteByBsSeq(int bsSeq) throws Exception {
        String sql = "delete from bs_users where bs_seq = ?";
        try (Connection con = this.getConnection();
             PreparedStatement statement = con.prepareStatement(sql)) {

            statement.setInt(1, bsSeq);
            statement.executeUpdate();

            con.commit();
        }
    }

    /**
     * 신규 사업자 회원 데이터 출력
     * @param start
     * @param end
     *
     */
    public List<BsUsersDTO> SelectByRangeForHost(int start, int end) throws Exception {
        String sql = "select  * from " +
                "(select bs_users.*, row_number() over(order by bs_signup desc) rn from bs_users) " +
                "where rn between ? and ?";
        try (Connection con = this.getConnection(); PreparedStatement pstat = con.prepareStatement(sql);) {
            pstat.setInt(1, start);
            pstat.setInt(2, end);

            try (ResultSet rs = pstat.executeQuery();) {
                List<BsUsersDTO> list = new ArrayList<BsUsersDTO>();
                while (rs.next()) {
                    list.add(new BsUsersDTO(rs));
                }
                return list;
            }
        }
    }


    // BsUsers 이름으로 검색한 총 게시글의 개수를 반환하는 코드
    public int getRecordCountByBsUsersName(String text) throws Exception {
        String sql = "select count(*) from bs_users where bs_name  like ? order by 1";
        try (Connection con = this.getConnection();
             PreparedStatement pstat = con.prepareStatement(sql);) {
            pstat.setString(1, "%" + text + "%");
            try (ResultSet rs = pstat.executeQuery();) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }




    // 이름 검색 네비
    public String getPageNaviByNameSearch(String text, int currentPage, int recordTotalCount) throws Exception {
        int recordCountPerPage = 10;
        int naviCountPerPage = 10;
        int pageTotalCount = 0;
        if (recordTotalCount % recordCountPerPage > 0) {
            pageTotalCount = (recordTotalCount / recordCountPerPage) + 1;
        } else {
            pageTotalCount = (recordTotalCount / recordCountPerPage);
        }
        if (currentPage < 1) {
            currentPage = 1;
        }
        if (currentPage > pageTotalCount) {
            currentPage = pageTotalCount;
        }
        int startNavi = (currentPage - 1) / recordCountPerPage * recordCountPerPage + 1;
        int endNavi = startNavi + naviCountPerPage - 1;
        if (endNavi > pageTotalCount) {
            endNavi = pageTotalCount;
        }
        boolean needPrev = true;
        boolean needNext = true;
        if (startNavi == 1) {
            needPrev = false;
        }
        if (endNavi == pageTotalCount) {
            needNext = false;
        }
        StringBuilder sb = new StringBuilder();
        if (needPrev) {
            sb.append("<li class=\"page-item\"><a class=\"page-link\" href='/bsUserSearch.host?cpage=" + (startNavi - 1) + "&type=board_title&inputT=" + text
                    + "'>Previous</a></li>");
        }
        for (int i = startNavi; i <= endNavi; i++) {
            if (currentPage == i) {
                sb.append("<li class=\"page-item active\" aria-current=\"page\"><a class=\"page-link\" href=\"/bsUserSearch.host?cpage=" + i + "&type=board_title&inputT=" + text + "\">" + i
                        + "</a></li>");
            } else {
                sb.append("<li class=\"page-item\"><a class=\"page-link\" href=\"/bsUserSearch.host?cpage=" + i + "&type=board_title&inputT=" + text + "\">" + i
                        + "</a></li>");
            }
        }
        if (needNext) {
            sb.append("<li class=\"page-item\"><a class=\"page-link\" href='/bsUserSearch.host?cpage=" + (endNavi + 1) + "&type=board_title&inputT=" + text
                    + "'>Next</a></li>");
        }
        return sb.toString();
    }











}
