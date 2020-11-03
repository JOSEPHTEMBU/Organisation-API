package models;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class DepartmentTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    public Department setUpDepartment(){
        Department department = new Department("Hospitality");
        return department;
    }
    @Test
    public void addDepartment_getName(){
        Department department = setUpDepartment();
        assertEquals("Hospitality",department.getName());
    }


    @Test
    public void addDepartment_getEmployees(){
        Department department = setUpDepartment();
        assertEquals(24,department.getEmployees());
    }


}