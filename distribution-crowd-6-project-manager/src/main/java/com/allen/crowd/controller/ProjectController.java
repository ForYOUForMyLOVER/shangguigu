package com.allen.crowd.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.allen.crowd.api.DataBaseOperationRemoteService;
import com.allen.crowd.api.RedisOperationRemoteService;
import com.allen.crowd.entity.ResultEntity;
import com.allen.crowd.entity.vo.MemberConfirmInfoVO;
import com.allen.crowd.entity.vo.ProjectVO;
import com.allen.crowd.entity.vo.ReturnVO;
import com.allen.crowd.util.CrowdConstant;
import com.allen.crowd.util.CrowdUtils;

@RestController
public class ProjectController {

	@Autowired
	private RedisOperationRemoteService redisOperationRemoteService;

	@Autowired
	private DataBaseOperationRemoteService dataBaseOperationRemoteService;

	@RequestMapping("/project/manager/initCreation")
	public ResultEntity<ProjectVO> initCreation(
			@RequestParam("memberSignToken") String memberSignToken) {

		// 1.检查是否登录，也就是检查memberSignToken是否有效
		ResultEntity<String> resultEntity = redisOperationRemoteService.retrieveStringValueByStringKey(memberSignToken);
		if(ResultEntity.FAILED.equals(resultEntity.getResult())) {
			return ResultEntity.failed(resultEntity.getMessage());
		}

		String memberId = resultEntity.getData();

		if(memberId == null) {
			return ResultEntity.failed(CrowdConstant.MESSAGE_ACCESS_DENIED);
		}

		// 2.创建空ProjectVO对象备用
		ProjectVO projectVO = new ProjectVO();

		// 3.将memberSignToken存入ProjectVO对象
		projectVO.setMemberSignToken(memberSignToken);

		// 4.将projectTempToken存入ProjectVO对象
		String projectTempToken = CrowdUtils.generateRedisKeyByPrefix(CrowdConstant.REDIS_PROJECT_TEMP_TOKEN_PREFIX);

		projectVO.setProjectTempToken(projectTempToken);

		// 5.将ProjectVO对象转换成JSON
		String jsonString = JSON.toJSONString(projectVO);

		// 6.将projectTempToken和jsonString作为key、value存入Redis
		// 虽然是临时数据，但是不能指定一个固定的过期时间，在用户操作完成时删除
		redisOperationRemoteService.saveNormalStringKeyValue(projectTempToken, jsonString, -1);

		return ResultEntity.successWithData(projectVO);
	}

	@RequestMapping("/project/manager/save/head/picture/path")
	public ResultEntity<String> saveHeadPicturePath(
			@RequestParam("memberSignToken") String memberSignToken,
			@RequestParam("projectTempToken") String projectTempToken,
			@RequestParam("headerPicturePath") String headerPicturePath
			) {

		// 检查是否登录，也就是检查memberSignToken是否有效
		ResultEntity<String> resultEntity = redisOperationRemoteService.retrieveStringValueByStringKey(memberSignToken);
		if(ResultEntity.FAILED.equals(resultEntity.getResult())) {
			return ResultEntity.failed(resultEntity.getMessage());
		}

		// project-manager工程访问Redis查询ProjectVO对象
		ResultEntity<String> resultEntityForGetValue = redisOperationRemoteService.retrieveStringValueByStringKey(projectTempToken);

		if(ResultEntity.FAILED.equals(resultEntityForGetValue.getResult())) {
			return ResultEntity.failed(resultEntityForGetValue.getMessage());
		}

		// 从Redis查询到JSON字符串
		String projectVOJSON = resultEntityForGetValue.getData();

		// 将JSON字符串还原成ProjectVO对象
		ProjectVO projectVO = JSON.parseObject(projectVOJSON, ProjectVO.class);

		// 将图片路径存入ProjectVO对象
		projectVO.setHeaderPicturePath(headerPicturePath);

		// 将ProjectVO对象转换为JSON字符串
		String jsonString = JSON.toJSONString(projectVO);

		// 将JSON字符串重新存入Redis
		return redisOperationRemoteService.saveNormalStringKeyValue(projectTempToken, jsonString, -1);

	}

	@RequestMapping("/project/manager/save/detail/picture/path/list")
	public ResultEntity<String> saveDetailPicturePathList(
			@RequestParam("memberSignToken") String memberSignToken,
			@RequestParam("projectTempToken") String projectTempToken,
			@RequestParam("detailPicturePathList") List<String> detailPicturePathList
			) {

		// 检查是否登录，也就是检查memberSignToken是否有效
		ResultEntity<String> resultEntity = redisOperationRemoteService.retrieveStringValueByStringKey(memberSignToken);
		if(ResultEntity.FAILED.equals(resultEntity.getResult())) {
			return ResultEntity.failed(resultEntity.getMessage());
		}

		// project-manager工程访问Redis查询ProjectVO对象
		ResultEntity<String> resultEntityForGetValue = redisOperationRemoteService.retrieveStringValueByStringKey(projectTempToken);

		if(ResultEntity.FAILED.equals(resultEntityForGetValue.getResult())) {
			return ResultEntity.failed(resultEntityForGetValue.getMessage());
		}

		// 从Redis查询到JSON字符串
		String projectVOJSON = resultEntityForGetValue.getData();

		// 将JSON字符串还原成ProjectVO对象
		ProjectVO projectVO = JSON.parseObject(projectVOJSON, ProjectVO.class);

		// 将图片路径存入ProjectVO对象
		projectVO.setDetailPicturePathList(detailPicturePathList);

		// 将ProjectVO对象转换为JSON字符串
		String jsonString = JSON.toJSONString(projectVO);

		// 将JSON字符串重新存入Redis
		return redisOperationRemoteService.saveNormalStringKeyValue(projectTempToken, jsonString, -1);

	}

	@RequestMapping("/project/manager/save/project/information")
	public ResultEntity<String> saveProjectInformation(@RequestBody ProjectVO projectVOFront) {

		// 获取memberSignToken
		String memberSignToken = projectVOFront.getMemberSignToken();

		// 检查是否登录，也就是检查memberSignToken是否有效
		ResultEntity<String> resultEntity = redisOperationRemoteService.retrieveStringValueByStringKey(memberSignToken);
		if(ResultEntity.FAILED.equals(resultEntity.getResult())) {
			return ResultEntity.failed(resultEntity.getMessage());
		}

		// 从projectVOFront中获取projectTempToken
		String projectTempToken = projectVOFront.getProjectTempToken();

		// project-manager工程访问Redis查询ProjectVO对象
		ResultEntity<String> resultEntityForGetValue = redisOperationRemoteService.retrieveStringValueByStringKey(projectTempToken);

		if(ResultEntity.FAILED.equals(resultEntityForGetValue.getResult())) {
			return ResultEntity.failed(resultEntityForGetValue.getMessage());
		}

		// 从Redis查询到JSON字符串
		String projectVOJSON = resultEntityForGetValue.getData();

		// 将JSON字符串还原成ProjectVO对象
		ProjectVO projectVOBehind = JSON.parseObject(projectVOJSON, ProjectVO.class);

		projectVOFront.setHeaderPicturePath(projectVOBehind.getHeaderPicturePath());
		projectVOFront.setDetailPicturePathList(projectVOBehind.getDetailPicturePathList());

		// 将projectVOFront对象中的属性复制到projectVOBehind对象
		BeanUtils.copyProperties(projectVOFront, projectVOBehind);

		// 将projectVOBehind对象转换为JSON字符串
		String jsonString = JSON.toJSONString(projectVOBehind);

		// 将JSON字符串重新存入Redis
		return redisOperationRemoteService.saveNormalStringKeyValue(projectTempToken, jsonString, -1);

	}

	@RequestMapping("/project/manager/save/return/information")
	public ResultEntity<String> saveReturnInformation(@RequestBody ReturnVO returnVO) {

		// 获取memberSignToken
		String memberSignToken = returnVO.getMemberSignToken();

		// 检查是否登录，也就是检查memberSignToken是否有效
		ResultEntity<String> resultEntity = redisOperationRemoteService.retrieveStringValueByStringKey(memberSignToken);
		if(ResultEntity.FAILED.equals(resultEntity.getResult())) {
			return ResultEntity.failed(resultEntity.getMessage());
		}

		// 从projectVOFront中获取projectTempToken
		String projectTempToken = returnVO.getProjectTempToken();

		// project-manager工程访问Redis查询ProjectVO对象
		ResultEntity<String> resultEntityForGetValue = redisOperationRemoteService.retrieveStringValueByStringKey(projectTempToken);

		if(ResultEntity.FAILED.equals(resultEntityForGetValue.getResult())) {
			return ResultEntity.failed(resultEntityForGetValue.getMessage());
		}

		// 从Redis查询到JSON字符串
		String projectVOJSON = resultEntityForGetValue.getData();

		// 将JSON字符串还原成ProjectVO对象
		ProjectVO projectVOBehind = JSON.parseObject(projectVOJSON, ProjectVO.class);

		//获取旧的汇报信息集合
		List<ReturnVO> returnVOList = projectVOBehind.getReturnVOList();
		if (!CrowdUtils.collectionEffectiveCheck(returnVOList)) {
			returnVOList = new ArrayList<>();
			projectVOBehind.setReturnVOList(returnVOList);
		}
		returnVOList.add(returnVO);

		// 将projectVOBehind对象转换为JSON字符串
		String jsonString = JSON.toJSONString(projectVOBehind);

		// 将JSON字符串重新存入Redis
		return redisOperationRemoteService.saveNormalStringKeyValue(projectTempToken, jsonString, -1);

	}

	@RequestMapping("/project/manager/save/confirm/information")
	public ResultEntity<String> saveConfirmInformation(@RequestBody MemberConfirmInfoVO memberConfirmInfoVO) {

		// 获取memberSignToken
		String memberSignToken = memberConfirmInfoVO.getMemberSignToken();

		// 检查是否登录，也就是检查memberSignToken是否有效
		ResultEntity<String> resultEntity = redisOperationRemoteService.retrieveStringValueByStringKey(memberSignToken);
		if(ResultEntity.FAILED.equals(resultEntity.getResult())) {
			return ResultEntity.failed(resultEntity.getMessage());
		}

		// 从projectVOFront中获取projectTempToken
		String projectTempToken = memberConfirmInfoVO.getProjectTempToken();

		// project-manager工程访问Redis查询ProjectVO对象
		ResultEntity<String> resultEntityForGetValue = redisOperationRemoteService.retrieveStringValueByStringKey(projectTempToken);

		if(ResultEntity.FAILED.equals(resultEntityForGetValue.getResult())) {
			return ResultEntity.failed(resultEntityForGetValue.getMessage());
		}

		// 从Redis查询到JSON字符串
		String projectVOJSON = resultEntityForGetValue.getData();

		// 将JSON字符串还原成ProjectVO对象
		ProjectVO projectVOBehind = JSON.parseObject(projectVOJSON, ProjectVO.class);

		projectVOBehind.setMemberConfirmInfoVO(memberConfirmInfoVO);

		String jsonString = JSON.toJSONString(projectVOBehind);

		// 将JSON字符串重新存入Redis
		return redisOperationRemoteService.saveNormalStringKeyValue(projectTempToken, jsonString, -1);

	}

	@RequestMapping("/project/manager/save/whole/project")
	public ResultEntity<String> saveWholeProject(
			@RequestParam("memberSignToken") String memberSignToken,
			@RequestParam("projectTempToken") String projectTempToken) {
		// 检查是否登录，也就是检查memberSignToken是否有效
		ResultEntity<String> resultEntity = redisOperationRemoteService.retrieveStringValueByStringKey(memberSignToken);
		if(ResultEntity.FAILED.equals(resultEntity.getResult())) {
			return ResultEntity.failed(resultEntity.getMessage());
		}

		String memberId = resultEntity.getData();
		if (memberId == null) {
			return ResultEntity.failed(CrowdConstant.MESSAGE_ACCESS_DENIED);
		}
		// project-manager工程访问Redis查询ProjectVO对象
		ResultEntity<String> resultEntityForGetValue = redisOperationRemoteService.retrieveStringValueByStringKey(projectTempToken);

		if(ResultEntity.FAILED.equals(resultEntityForGetValue.getResult())) {
			return ResultEntity.failed(resultEntityForGetValue.getMessage());
		}
		// 从Redis查询到JSON字符串
		String projectVOJSON = resultEntityForGetValue.getData();
		// 将JSON字符串还原成ProjectVO对象
		ProjectVO projectVO = JSON.parseObject(projectVOJSON, ProjectVO.class);
		ResultEntity<String> resultEntityForSave = dataBaseOperationRemoteService.saveProjectRemote(projectVO ,memberId);
		
		if(ResultEntity.FAILED.equals(resultEntityForSave.getResult())) {
			return resultEntityForSave;
		}
		
		// 删除Redis中的临时数据
		return redisOperationRemoteService.removeByKey(projectTempToken);
	}
}
