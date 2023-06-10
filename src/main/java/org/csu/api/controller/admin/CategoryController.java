package org.csu.api.controller.admin;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.csu.api.common.CONSTANT;
import org.csu.api.common.CommonResponse;
import org.csu.api.domain.Category;
import org.csu.api.service.CategoryService;
import org.csu.api.vo.CategoryVO;
import org.csu.api.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/category/")
@Validated
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("getCategory")
    public CommonResponse<CategoryVO> getCategoryById(
            @RequestParam Integer categoryId,
            HttpSession session){
        UserVO userVO = (UserVO) session.getAttribute(CONSTANT.LOGIN_USER);
        if(userVO.getRole()==CONSTANT.ROLE.ADMIN){
            return categoryService.getCategory(categoryId);
        }
        else return CommonResponse.createForError("没有此权限");
    }

    @GetMapping("get_children_categories")
    public CommonResponse<List<CategoryVO>> getChildrenCategories(
            @RequestParam(defaultValue = "0") Integer categoryId,
            HttpSession session){
        UserVO userVO = (UserVO) session.getAttribute(CONSTANT.LOGIN_USER);
        if(userVO.getRole()==CONSTANT.ROLE.ADMIN){
            return categoryService.getChildrenCategories(categoryId);
        }
        else return CommonResponse.createForError("没有此权限");
    }

    @GetMapping("get_all_children_categories")
    public CommonResponse<List<Integer>> getAllChildrenCategories(
            @RequestParam(defaultValue = "0") Integer categoryId,
            HttpSession session){
        UserVO userVO = (UserVO) session.getAttribute(CONSTANT.LOGIN_USER);
        if(userVO.getRole()==CONSTANT.ROLE.ADMIN){
            return categoryService.getCategoryAndAllChildren(categoryId);
        }
        else return CommonResponse.createForError("没有此权限");
    }
    //查看根节点类别

}
