package com.project.JewelHub.util;

import com.project.JewelHub.items.ItemDto;
import com.project.JewelHub.items.Item;
import com.project.JewelHub.user.User;
import com.project.JewelHub.user.dtos.UserDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CustomMapper {
    public static UserDto mapUserDto(User user){

        return new UserDto(user.getUserId(),user.getFirstname(), user.getLastname(), user.getEmail(), user.getPassword(), user.getRole(), user.getContact());
    }

    public static List<UserDto> mapUserDtos(List<User> users) {

        List<UserDto> userDtos = new ArrayList<>();
        for (User user : users) {
            userDtos.add(CustomMapper.mapUserDto(user));
        }
        return userDtos;
    }

    public static List<ItemDto> mapItemDto(List<Item> items) {

        List<ItemDto> itemDtos = new ArrayList<>();
        for (Item item : items) {
            itemDtos.add(new ItemDto(
                    item.getItemCode(),
                    item.getItemName(),
                    item.getMaterial(),
                    item.getKarat(),
                    item.getGrossWeight(),
                    item.getWastage(),
                    item.getNetWeight(),
                    item.getGoldPrice(),
                    item.getCostOfStone(),
                    item.getManufactureCost(),
                    item.getDescription(),
                    item.getTotalCost()
            ));
        }
        return itemDtos;
    }
}





