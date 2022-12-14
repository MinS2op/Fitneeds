package controllers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.oreilly.servlet.MultipartRequest;
import commons.FileControl;
import dao.*;
import dto.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@WebServlet("*.bsPage")
public class BsPageController extends ControllerAbs {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        super.doGet(request, response);

        String uri = request.getRequestURI();

        try {
            switch (uri) {
                // 사업자 페이지 열기
                case "/page.bsPage":
                    this.getPage(request, response);
                    request.getRequestDispatcher("/bs/bs-page.jsp").forward(request, response);
                    break;
                // 프로필 업데이트
                case "/updateProfile.bsPage":
                    this.updateProfile(request, response);
                    break;
                // 사업증 업데이트
                case "/updateCtfc.bsPage":
                    this.updateCtfc(request, response);
                    break;
                // 비밀번호 변경
                case "/updatePw.bsPage":
                    this.updatePw(request, response);
                    break;
                //회원탈퇴
                case "/signDown.bsPage":
                    this.signDown(request, response);
                    request.getSession().removeAttribute("bsSeq");
                    response.sendRedirect("/");
                    break;
                // 시설 업데이트 페이지로 이동
                case "/toUpdateGym.bsPage":
                    this.importGym(request, response);
                    request.getRequestDispatcher("/gym/gym-modify.jsp").forward(request, response);
                    break;
                // 시설 업데이트
                case "/updateGym.bsPage":
                    this.updateGymInfo(request, response);
                    response.sendRedirect("/page.bsPage");
                    break;
                // 시설 삭제
                case "/deleteGym.bsPage":
                    this.deleteGym(request, response);
                    response.sendRedirect("/page.bsPage");
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("/");
        }

    }


    /**
     * <h2>gymSeq로 관련된 데이터를 지움</h2>
     *
     * @param request for gym_seq
     */
    private void deleteGym(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int gymSeq = Integer.parseInt(request.getParameter("gym_seq"));

        // 시설 필터 gym_filter table 지우기
        GymFilterDAO.getInstance().deleteByGymSeq(gymSeq);
        // 시설 이미지 gym_img 지우기
        GymImgDAO.getInstance().deleteByGymSeq(gymSeq);
        // 즐겨찾기 favorite table 지우기
        FavoritesDAO.getInstance().deleteByGymSeq(gymSeq);
        // 리뷰 좋아요 likes 지우기
        LikesDAO.getInstance().deleteByGymSeq(gymSeq);
        // 리뷰 review table 지우기
        ReviewDAO.getInstance().deleteByGymSeq(gymSeq);
        // 시설 gym table 지우기
        GymDAO.getInstance().deleteByGymSeq(gymSeq);
    }

    /**
     * <h2>gym데이터를 request에 담음</h2>
     */
    public void importGym(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int gymSeq = Integer.parseInt(request.getParameter("gym_seq"));
        request.setAttribute("gymSeq", gymSeq);
        GymDTO gym = GymDAO.getInstance().printGym(gymSeq);
        GymFilterDTO gymFilter = GymFilterDAO.getInstance().selectByGymSeq(gymSeq);

        GymImgDTO gymImg = GymImgDAO.getInstance().getByGymSeq(gymSeq);
        Gson gson = new Gson();

        Type type = new TypeToken<String[]>() {
        }.getType();
        String[] gymImgList = gson.fromJson(gymImg.getGym_sysimg(), type);

        request.setAttribute("gymImgList", gymImgList);
        request.setAttribute("gym", gym);
        request.setAttribute("gymFilter", gymFilter);
    }

    /**
     * <h2>사업자 회원 탈퇴</h2>
     */
    private void signDown(HttpServletRequest request, HttpServletResponse response) throws Exception {

        int bsSeq = (Integer) request.getSession().getAttribute("bsSeq");
        List<GymDTO> gymList = GymDAO.getInstance().getGymByBsSeq(bsSeq);

        // 광고배너 지우기
        AdDAO.getInstance().deleteByBsSeq(bsSeq);

        // 즐겨찾기 지우기
        FavoritesDAO favDao = FavoritesDAO.getInstance();

        for (GymDTO gym : gymList) {
            favDao.deleteByGymSeq(gym.getGym_seq());
        }


        // 리뷰 좋아요 지우기
        ReviewDAO.getInstance().deleteByBsSeq(bsSeq);

        // 사업자 등록증 지우기
        BsCtfcDAO.getInstance().deleteByBsSeq(bsSeq);

        // 시설 필터 지우기
        GymFilterDAO gymFilDao = GymFilterDAO.getInstance();

        for (GymDTO gym : gymList) {
            gymFilDao.deleteByGymSeq(gym.getGym_seq());
        }

        // 시설이미지 지우기
        FileControl file = new FileControl();
        GymImgDAO gymImgDAO = GymImgDAO.getInstance();

        for (GymDTO gym : gymList) {
            GymImgDTO gymImg = gymImgDAO.getByGymSeq(gym.getGym_seq());
            Gson gson = new Gson();
            List<String> gymImgList;

            gymImgList = gson.fromJson(gymImg.getGym_sysimg(), new TypeToken<List<String>>() {
            }.getType());
            if (gymImgList == null) {
                gymImgList = new ArrayList<>();
            }

            for (String gymName : gymImgList) {
                file.delete(request, "/resource/gym", gymName);
            }
            GymImgDAO.getInstance().deleteByGymSeq(gym.getGym_seq());
        }
        //시설 지우기
        GymDAO.getInstance().deleteByBsSeq(bsSeq);
        // 비지니스 유저 지유기
        BsUsersDAO.getInstance().deleteByBsSeq(bsSeq);
    }

    /**
     * <h2>비밀번호 변경</h2>
     */
    private void updatePw(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int bsSeq = (Integer) request.getSession().getAttribute("bsSeq");
        String newPw = request.getParameter("pw");
        BsUsersDAO.getInstance().updatePw(bsSeq, newPw);
    }

    /**
     * <h1>사업자 등록증 수정</h1>
     * 기존의 사업증 이미지를 삭제하고 새로운 사업증 이미지를 생성하고 등록함.
     */
    private void updateCtfc(HttpServletRequest request, HttpServletResponse response) throws Exception {
        FileControl fileControl = new FileControl();

        int bsSeq = (Integer) request.getSession().getAttribute("bsSeq");
        String path = "/resource/ctfc";
        String oldName = BsCtfcDAO.getInstance().getByBsSeq(bsSeq).getSysName();

        // 파일 삭제
        fileControl.delete(request, path, oldName);

        // 파일 생성
        String newName = fileControl.save(request, path, "bs_ctfc_img");

        BsCtfcDTO bsCtfc = new BsCtfcDTO(bsSeq, null, newName);

        BsCtfcDAO.getInstance().updateSysName(bsCtfc);

    }

    /**
     * <h1>사업자 프로필 수정</h1>
     * bs_users, bs_ctfc 데이터 베이스 수정
     */
    private void updateProfile(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int bsSeq = (Integer) request.getSession().getAttribute("bsSeq");
        String name = request.getParameter("name");
        String phone = request.getParameter("phone");
        String number = request.getParameter("number");

        BsUsersDTO bsUser = new BsUsersDTO();

        bsUser.setBs_seq(bsSeq);
        bsUser.setBs_name(name);
        bsUser.setBs_phone(phone);
        bsUser.setBs_number(number);

        BsUsersDAO.getInstance().updateProfile(bsUser);
        BsCtfcDAO.getInstance().updateBsNum(new BsCtfcDTO(bsSeq, number, null));
    }


    /**
     * <h1>사업자 페이지 데이터 불러오기</h1>
     * session에 bsSeq만 필요
     */
    private void getPage(HttpServletRequest request, HttpServletResponse response) throws Exception {

        int bsSeq = (Integer) request.getSession().getAttribute("bsSeq");

        List<GymDTO> gymList = GymDAO.getInstance().getGymByBsSeq(bsSeq);
        List<GymFilterDTO> gymFilterList = new ArrayList<>();
        GymFilterDAO filterDAO = GymFilterDAO.getInstance();
        for (GymDTO gym : gymList) {
            gymFilterList.add(filterDAO.selectByGymSeq(gym.getGym_seq()));
        }

        BsUsersDTO bsUser = BsUsersDAO.getInstance().getByBsSeq(bsSeq);
        BsCtfcDTO bsCtfc = BsCtfcDAO.getInstance().getByBsSeq(bsSeq);

        request.setAttribute("bsUser", bsUser);
        request.setAttribute("bsCtfc", bsCtfc);
        request.setAttribute("gymList", gymList);
        request.setAttribute("gymFilterList", gymFilterList);
    }


    /**
     * <h1>시설정보 및 시설필터 수정하기</h1>
     */
    private void updateGymInfo(HttpServletRequest request, HttpServletResponse response) throws Exception {

        FileControl file = new FileControl();
        // gymImg data
        List<String> newImgList = file.saves(request, "/resource/gym");
        MultipartRequest multi = file.getMulti();
        int gymSeq = Integer.parseInt(multi.getParameter("gymSeq"));
        Type listStringType = new TypeToken<List<String>>() {
        }.getType();
        Gson gson = new Gson();

        // 기존 이미지 리스트
        List<String> beforeImgList = gson.fromJson(GymImgDAO.getInstance().getByGymSeq(gymSeq).getGym_sysimg(), listStringType);
        boolean checkChangMainImg = false;
        String mainImg = "";
        if(beforeImgList != null && beforeImgList.size() != 0) {
            mainImg = beforeImgList.get(0);
        }else {
            beforeImgList = new ArrayList<>();
        }
        // 지울 파일 리스트
        List<String> delImgList = gson.fromJson(multi.getParameter("del_img_list"), listStringType);

        // 선택한 파일 지우기
        if(delImgList != null) {
            for (String delImg : delImgList) {
                if (delImg.endsWith(mainImg)) {
                    checkChangMainImg = true;
                }
                String savePath = request.getServletContext().getRealPath(delImg);
                File delFile = new File(savePath);
                delFile.delete();
                String rm = delImg.replaceAll(".*/", "");
                beforeImgList.remove(rm);
            }
        }

        // 최신 이미지 리스트
        List<String> afterImgList = new ArrayList<>();
        if(checkChangMainImg){
            // 메인이 지워졌을 때
            afterImgList.addAll(newImgList);
            afterImgList.addAll(beforeImgList);
        }else{
            // 메인이 안 지워졌을 때
            afterImgList.addAll(beforeImgList);
            afterImgList.addAll(newImgList);
        }

        // gymFilter data
        String open = multi.getParameter("open_result");
        String locker = multi.getParameter("locker_result");
        String shower = multi.getParameter("shower_result");
        String park = multi.getParameter("park_result");

        // gym data
        GymDTO gymDTO = new GymDTO(file);
        if (multi.getParameter("address1") == null) {
            GymDTO beforeGym = GymDAO.getInstance().printGym(gymDTO.getGym_seq());
            gymDTO.setGym_location(beforeGym.getGym_location());
        }

        if(afterImgList.size() != 0) {
            gymDTO.setGym_main_sysImg(afterImgList.get(0));
        }else{
            gymDTO.setGym_main_sysImg("");
        }

        GymFilterDTO gymFilterDTO = new GymFilterDTO(gymSeq, open, locker, shower, park);

        String json = gson.toJson(afterImgList);

        GymImgDAO.getInstance().update(gymSeq, json);
        GymDAO.getInstance().updateGym(gymDTO);
        GymFilterDAO.getInstance().updateGymFilter(gymFilterDTO);
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        this.doGet(request, response);
    }
}
