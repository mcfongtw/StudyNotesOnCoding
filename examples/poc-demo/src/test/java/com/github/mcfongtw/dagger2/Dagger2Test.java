package com.github.mcfongtw.dagger2;

import com.github.mcfongtw.dagger2.wiring.AppComponent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class Dagger2Test {

    private AppComponent mockAppComponent;

    @Mock
    private Dao1Impl dao1;

    @Mock
    private Dao2Impl dao2;

    @BeforeEach
    public void setup() {
        mockAppComponent = new AppComponent() {
            @Override
            public void inject(SubClass subClass) {
                subClass.dao1 = dao1;
                subClass.dao2 = dao2;
            }
        };
    }

    @Test
    public void testInjectSubClass() {
        SubClass subClass = new SubClass(mockAppComponent);

        Assertions.assertEquals(subClass.dao1, dao1);
        Assertions.assertEquals(subClass.dao2, dao2);
    }
}
