package com.defect.tracker.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.defect.tracker.repositories.SubModuleRepository;
import com.defect.tracker.service.SubModuleService;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.defect.tracker.common.response.PaginatedContentResponse.Pagination;
import com.defect.tracker.entities.Module;
import com.defect.tracker.entities.QModule;
import com.defect.tracker.entities.QSubModule;
import com.defect.tracker.entities.SubModule;
import com.defect.tracker.response.dto.SubModuleResponse;
import com.defect.tracker.resquest.dto.SubModuleRequest;
import com.defect.tracker.search.response.SubModuleSearch;
import com.defect.tracker.utils.Utils;
import com.querydsl.core.BooleanBuilder;

@Service
public class SubModleServiceImpl implements SubModuleService {
  @Autowired
  private SubModuleRepository subModuleRepository;

  @Transactional
  public void saveSubModule(SubModuleRequest subModuleRequest) {
    SubModule subModule = new SubModule();
    BeanUtils.copyProperties(subModuleRequest, subModule);
    Module module = new Module();
    module.setId(subModuleRequest.getModuleId());
    subModule.setModule(module);
    subModuleRepository.save(subModule);
  }

  @Transactional
  public List<SubModuleResponse> getAllSubModule() {
    List<SubModuleResponse> subModuleResponses = new ArrayList<>();
    List<SubModule> subModules = subModuleRepository.findAll();
    for (SubModule subModule : subModules) {
      SubModuleResponse subModuleResponse = new SubModuleResponse();
      BeanUtils.copyProperties(subModule, subModuleResponse);
      subModuleResponse.setModuleId(subModule.getModule().getId());
      subModuleResponse.setModuleName(subModule.getModule().getName());
      subModuleResponses.add(subModuleResponse);
    }
    return subModuleResponses;
  }

  @Override
  public boolean isSubModuleExists(String name) {
    return subModuleRepository.existsByNameIgnoreCase(name);
  }

  @Transactional
  public SubModuleResponse getSubModuleById(Long id) {
    SubModule subModule = subModuleRepository.findById(id).get();
    SubModuleResponse subModuleResponse = new SubModuleResponse();
    BeanUtils.copyProperties(subModule, subModuleResponse);
    subModuleResponse.setModuleId(subModule.getModule().getId());
    subModuleResponse.setModuleName(subModule.getModule().getName());
    return subModuleResponse;
  }

  @Override
  public boolean existsBySubModule(Long id) {
    return subModuleRepository.existsById(id);
  }

  @Override
  public boolean existsByModuleIdAndId(Long moduleId, Long id) {
    return subModuleRepository.existsByModuleIdAndId(moduleId, id);
  }

  public boolean isUpdatedSubModuleNameExist(Long id, String name) {
    return subModuleRepository.existsByNameIgnoreCaseAndIdNot(name, id);
  }

  @Override
  public void deleteSubModule(Long id) {
    subModuleRepository.deleteById(id);
  }

  @Override
  public List<SubModule> getAllSubModuleByModuleId(Long id) {
    return subModuleRepository.findByModuleId(id);
  }

  @Override
  public List<SubModuleResponse> multiSearchSubModule(Pageable pageable, Pagination pagination,
      SubModuleSearch subModuleSearch) {
    BooleanBuilder booleanBuilder = new BooleanBuilder();
    QSubModule qSubModule = QSubModule.subModule;
    if (Utils.isNotNullAndEmpty(subModuleSearch.getName())) {
      booleanBuilder.and(qSubModule.name.containsIgnoreCase(subModuleSearch.getName()));
    }
    if (Utils.isNotNullAndEmpty(subModuleSearch.getModule())) {
      QModule qModule = QModule.module;
      booleanBuilder.and(qModule.name.containsIgnoreCase(subModuleSearch.getModule()));
    }
    List<SubModuleResponse> subModuleResponses = new ArrayList<>();
    Page<SubModule> subModules = subModuleRepository.findAll(booleanBuilder, pageable);
    pagination.setTotalRecords(subModules.getTotalElements());
    pagination.setTotalPages(subModules.getTotalPages());
    for (SubModule subModule : subModules.toList()) {
      SubModuleResponse subModuleResponse = new SubModuleResponse();
      BeanUtils.copyProperties(subModule, subModuleResponse);
      subModuleResponse.setModuleId(subModule.getModule().getId());
      subModuleResponse.setModuleName(subModule.getModule().getName());
      subModuleResponses.add(subModuleResponse);
    }
    return subModuleResponses;
  }

  @Override
  public boolean existsByModuleId(Long id) {
    return subModuleRepository.existsByModuleId(id);
  }

  @Override
  public boolean existBySubModule(Long id) {
    return subModuleRepository.existsByModuleId(id);
  }

  @Override
  public void exportSubModuleToCsv(PrintWriter writer) {
    List<SubModule> subModules = subModuleRepository.findAll();
    writer.write("SubModuleID,SubModuleName,Module\n");
    for (SubModule subModule : subModules) {
      SubModuleResponse subModuleResponse = new SubModuleResponse();
      BeanUtils.copyProperties(subModule, subModuleResponse);
      subModuleResponse.setModuleId(subModule.getModule().getId());
      subModuleResponse.setModuleName(subModule.getModule().getName());
      writer.write(subModuleResponse.getId() + "," + subModuleResponse.getName() + ","
          + subModuleResponse.getModuleName() + "\n");
    }
  }

  @Transactional
  public void importSubModuleByCsv(MultipartFile file) {

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
      String line;
      while ((line = reader.readLine()) != null) {
        String data[] = line.split(",");
        SubModuleRequest subModuleRequest = new SubModuleRequest();
        Module module = new Module();
        SubModule subModule = new SubModule();
        subModule.setName(data[1]);
        subModuleRequest.setModuleId(Long.parseLong(data[2]));
        module.setId(subModuleRequest.getModuleId());
        subModule.setModule(module);
        BeanUtils.copyProperties(subModule, subModuleRequest);
        subModuleRepository.save(subModule);
      }
    } catch (Exception ex) {
      System.out.println("tiyan");
    }
  }
}
