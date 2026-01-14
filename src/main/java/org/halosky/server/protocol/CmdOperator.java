package org.halosky.server.protocol;

import lombok.Getter;

/**
 * packageName org.halosky.server.protocol
 *
 * @author huan.yang
 * @className CmdOperator
 * @date 2026/1/13
 */
@Getter
public enum CmdOperator {
    ADD_DOCUMENT(1,"add doc"),
    DEL_DOCUMENT(2,"del doc"),
    UPD_DOCUMENT(3,"update doc"),
    FET_DOCUMENT(4,"query doc"),
    ADD_INDEX(5,"add the index"),
    DEL_INDEX(6,"delete the index"),
    RES_DOCS(7,"result for FET_DOCUMENT")

    ;

    private final int code;

    private final String msg;

    CmdOperator(int code,String msg) {
        this.code = code;
        this.msg = msg;
    }


    public static CmdOperator getByCode(int code){
        for (CmdOperator value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return null;
    }

}
