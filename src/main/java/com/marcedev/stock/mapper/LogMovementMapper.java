package com.marcedev.stock.mapper;

import com.marcedev.stock.dto.LogMovementDto;
import com.marcedev.stock.entity.LogMovement;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LogMovementMapper {

    LogMovementDto toDto(LogMovement entity);

    List<LogMovementDto> toDtoList(List<LogMovement> list);
}
