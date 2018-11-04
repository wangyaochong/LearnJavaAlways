package mapper;

import entity.UserM;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface UserMapperWithAnno {
    /**
     * 新增用戶
     *
     * @param user
     * @return
     * @
     */
    @Insert("insert into user (name,age) values (#{name},#{age})")
    int insertUser(UserM user);

    /**
     * 修改用戶
     *
     * @param user
     * @param id
     * @return
     * @
     */
    @Update("update user set name=#{name},age=#{age} where id=#{id}")
    int updateUser(UserM user, int id);

    /**
     * 刪除用戶
     *
     * @param id
     * @return
     * @
     */
    @Delete("  delete from user where id=#{id}")
    int deleteUser(int id);

    /**
     * 根据id查询用户信息
     *
     * @param id
     * @return
     * @
     */
    @Select("  select * from user where id=#{id}")
    UserM selectUserById(int id);

    /**
     * 查询所有的用户信息
     *
     * @return
     * @
     */
    @Select("select * from user")
    List<UserM> selectAllUser();
}
