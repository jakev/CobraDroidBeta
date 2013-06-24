/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dxc.junit.opcodes;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Listing of all the tests that are to be run.
 */
public class AllTests {

    public static void run() {
        TestRunner.main(new String[] {AllTests.class.getName()});
    }

    public static final Test suite() {
        TestSuite suite = new TestSuite("Tests for all java vm opcodes");
        suite.addTestSuite(dxc.junit.opcodes.aaload.Test_aaload.class);
        suite.addTestSuite(dxc.junit.opcodes.aastore.Test_aastore.class);
        suite
                .addTestSuite(dxc.junit.opcodes.aconst_null.Test_aconst_null.class);
        suite.addTestSuite(dxc.junit.opcodes.aload.Test_aload.class);
        suite.addTestSuite(dxc.junit.opcodes.aload_0.Test_aload_0.class);
        suite.addTestSuite(dxc.junit.opcodes.aload_1.Test_aload_1.class);
        suite.addTestSuite(dxc.junit.opcodes.aload_2.Test_aload_2.class);
        suite.addTestSuite(dxc.junit.opcodes.aload_3.Test_aload_3.class);
        suite.addTestSuite(dxc.junit.opcodes.anewarray.Test_anewarray.class);
        suite.addTestSuite(dxc.junit.opcodes.areturn.Test_areturn.class);
        suite
                .addTestSuite(dxc.junit.opcodes.arraylength.Test_arraylength.class);
        suite.addTestSuite(dxc.junit.opcodes.astore.Test_astore.class);
        suite.addTestSuite(dxc.junit.opcodes.astore_0.Test_astore_0.class);
        suite.addTestSuite(dxc.junit.opcodes.astore_1.Test_astore_1.class);
        suite.addTestSuite(dxc.junit.opcodes.astore_2.Test_astore_2.class);
        suite.addTestSuite(dxc.junit.opcodes.astore_3.Test_astore_3.class);
        suite.addTestSuite(dxc.junit.opcodes.athrow.Test_athrow.class);
        suite.addTestSuite(dxc.junit.opcodes.baload.Test_baload.class);
        suite.addTestSuite(dxc.junit.opcodes.bastore.Test_bastore.class);
        suite.addTestSuite(dxc.junit.opcodes.bipush.Test_bipush.class);
        suite.addTestSuite(dxc.junit.opcodes.caload.Test_caload.class);
        suite.addTestSuite(dxc.junit.opcodes.castore.Test_castore.class);
        suite.addTestSuite(dxc.junit.opcodes.checkcast.Test_checkcast.class);
        suite.addTestSuite(dxc.junit.opcodes.d2f.Test_d2f.class);
        suite.addTestSuite(dxc.junit.opcodes.d2i.Test_d2i.class);
        suite.addTestSuite(dxc.junit.opcodes.d2l.Test_d2l.class);
        suite.addTestSuite(dxc.junit.opcodes.dadd.Test_dadd.class);
        suite.addTestSuite(dxc.junit.opcodes.daload.Test_daload.class);
        suite.addTestSuite(dxc.junit.opcodes.dastore.Test_dastore.class);
        suite.addTestSuite(dxc.junit.opcodes.dcmpg.Test_dcmpg.class);
        suite.addTestSuite(dxc.junit.opcodes.dcmpl.Test_dcmpl.class);
        suite.addTestSuite(dxc.junit.opcodes.dconst_0.Test_dconst_0.class);
        suite.addTestSuite(dxc.junit.opcodes.dconst_1.Test_dconst_1.class);
        suite.addTestSuite(dxc.junit.opcodes.ddiv.Test_ddiv.class);
        suite.addTestSuite(dxc.junit.opcodes.dload.Test_dload.class);
        suite.addTestSuite(dxc.junit.opcodes.dload_0.Test_dload_0.class);
        suite.addTestSuite(dxc.junit.opcodes.dload_1.Test_dload_1.class);
        suite.addTestSuite(dxc.junit.opcodes.dload_2.Test_dload_2.class);
        suite.addTestSuite(dxc.junit.opcodes.dload_3.Test_dload_3.class);
        suite.addTestSuite(dxc.junit.opcodes.dmul.Test_dmul.class);
        suite.addTestSuite(dxc.junit.opcodes.dneg.Test_dneg.class);
        suite.addTestSuite(dxc.junit.opcodes.drem.Test_drem.class);
        suite.addTestSuite(dxc.junit.opcodes.dreturn.Test_dreturn.class);
        suite.addTestSuite(dxc.junit.opcodes.dstore.Test_dstore.class);
        suite.addTestSuite(dxc.junit.opcodes.dstore_0.Test_dstore_0.class);
        suite.addTestSuite(dxc.junit.opcodes.dstore_1.Test_dstore_1.class);
        suite.addTestSuite(dxc.junit.opcodes.dstore_2.Test_dstore_2.class);
        suite.addTestSuite(dxc.junit.opcodes.dstore_3.Test_dstore_3.class);
        suite.addTestSuite(dxc.junit.opcodes.dsub.Test_dsub.class);
        suite.addTestSuite(dxc.junit.opcodes.dup.Test_dup.class);
        suite.addTestSuite(dxc.junit.opcodes.dup_x1.Test_dup_x1.class);
        suite.addTestSuite(dxc.junit.opcodes.dup_x2.Test_dup_x2.class);
        suite.addTestSuite(dxc.junit.opcodes.dup2.Test_dup2.class);
        suite.addTestSuite(dxc.junit.opcodes.dup2_x1.Test_dup2_x1.class);
        suite.addTestSuite(dxc.junit.opcodes.dup2_x2.Test_dup2_x2.class);
        suite.addTestSuite(dxc.junit.opcodes.f2d.Test_f2d.class);
        suite.addTestSuite(dxc.junit.opcodes.f2i.Test_f2i.class);
        suite.addTestSuite(dxc.junit.opcodes.f2l.Test_f2l.class);
        suite.addTestSuite(dxc.junit.opcodes.fadd.Test_fadd.class);
        suite.addTestSuite(dxc.junit.opcodes.faload.Test_faload.class);
        suite.addTestSuite(dxc.junit.opcodes.fastore.Test_fastore.class);
        suite.addTestSuite(dxc.junit.opcodes.fcmpg.Test_fcmpg.class);
        suite.addTestSuite(dxc.junit.opcodes.fcmpl.Test_fcmpl.class);
        suite.addTestSuite(dxc.junit.opcodes.fconst_0.Test_fconst_0.class);
        suite.addTestSuite(dxc.junit.opcodes.fconst_1.Test_fconst_1.class);
        suite.addTestSuite(dxc.junit.opcodes.fconst_2.Test_fconst_2.class);
        suite.addTestSuite(dxc.junit.opcodes.fdiv.Test_fdiv.class);
        suite.addTestSuite(dxc.junit.opcodes.fload.Test_fload.class);
        suite.addTestSuite(dxc.junit.opcodes.fload_0.Test_fload_0.class);
        suite.addTestSuite(dxc.junit.opcodes.fload_1.Test_fload_1.class);
        suite.addTestSuite(dxc.junit.opcodes.fload_2.Test_fload_2.class);
        suite.addTestSuite(dxc.junit.opcodes.fload_3.Test_fload_3.class);
        suite.addTestSuite(dxc.junit.opcodes.fmul.Test_fmul.class);
        suite.addTestSuite(dxc.junit.opcodes.fneg.Test_fneg.class);
        suite.addTestSuite(dxc.junit.opcodes.frem.Test_frem.class);
        suite.addTestSuite(dxc.junit.opcodes.freturn.Test_freturn.class);
        suite.addTestSuite(dxc.junit.opcodes.fstore.Test_fstore.class);
        suite.addTestSuite(dxc.junit.opcodes.fstore_0.Test_fstore_0.class);
        suite.addTestSuite(dxc.junit.opcodes.fstore_1.Test_fstore_1.class);
        suite.addTestSuite(dxc.junit.opcodes.fstore_2.Test_fstore_2.class);
        suite.addTestSuite(dxc.junit.opcodes.fstore_3.Test_fstore_3.class);
        suite.addTestSuite(dxc.junit.opcodes.fsub.Test_fsub.class);
        suite.addTestSuite(dxc.junit.opcodes.getfield.Test_getfield.class);
        suite.addTestSuite(dxc.junit.opcodes.getstatic.Test_getstatic.class);
        suite.addTestSuite(dxc.junit.opcodes.opc_goto.Test_opc_goto.class);
        suite.addTestSuite(dxc.junit.opcodes.goto_w.Test_goto_w.class);
        suite.addTestSuite(dxc.junit.opcodes.i2b.Test_i2b.class);
        suite.addTestSuite(dxc.junit.opcodes.i2c.Test_i2c.class);
        suite.addTestSuite(dxc.junit.opcodes.i2d.Test_i2d.class);
        suite.addTestSuite(dxc.junit.opcodes.i2f.Test_i2f.class);
        suite.addTestSuite(dxc.junit.opcodes.i2l.Test_i2l.class);
        suite.addTestSuite(dxc.junit.opcodes.i2s.Test_i2s.class);
        suite.addTestSuite(dxc.junit.opcodes.iaload.Test_iaload.class);
        suite.addTestSuite(dxc.junit.opcodes.iadd.Test_iadd.class);
        suite.addTestSuite(dxc.junit.opcodes.iand.Test_iand.class);
        suite.addTestSuite(dxc.junit.opcodes.iastore.Test_iastore.class);
        suite.addTestSuite(dxc.junit.opcodes.iconst_m1.Test_iconst_m1.class);
        suite.addTestSuite(dxc.junit.opcodes.iconst_0.Test_iconst_0.class);
        suite.addTestSuite(dxc.junit.opcodes.iconst_1.Test_iconst_1.class);
        suite.addTestSuite(dxc.junit.opcodes.iconst_2.Test_iconst_2.class);
        suite.addTestSuite(dxc.junit.opcodes.iconst_3.Test_iconst_3.class);
        suite.addTestSuite(dxc.junit.opcodes.iconst_4.Test_iconst_4.class);
        suite.addTestSuite(dxc.junit.opcodes.iconst_5.Test_iconst_5.class);
        suite.addTestSuite(dxc.junit.opcodes.idiv.Test_idiv.class);
        suite.addTestSuite(dxc.junit.opcodes.if_acmpeq.Test_if_acmpeq.class);
        suite.addTestSuite(dxc.junit.opcodes.if_acmpne.Test_if_acmpne.class);
        suite.addTestSuite(dxc.junit.opcodes.if_icmpeq.Test_if_icmpeq.class);
        suite.addTestSuite(dxc.junit.opcodes.if_icmpge.Test_if_icmpge.class);
        suite.addTestSuite(dxc.junit.opcodes.if_icmpgt.Test_if_icmpgt.class);
        suite.addTestSuite(dxc.junit.opcodes.if_icmple.Test_if_icmple.class);
        suite.addTestSuite(dxc.junit.opcodes.if_icmplt.Test_if_icmplt.class);
        suite.addTestSuite(dxc.junit.opcodes.if_icmpne.Test_if_icmpne.class);
        suite.addTestSuite(dxc.junit.opcodes.ifeq.Test_ifeq.class);
        suite.addTestSuite(dxc.junit.opcodes.ifge.Test_ifge.class);
        suite.addTestSuite(dxc.junit.opcodes.ifgt.Test_ifgt.class);
        suite.addTestSuite(dxc.junit.opcodes.ifle.Test_ifle.class);
        suite.addTestSuite(dxc.junit.opcodes.iflt.Test_iflt.class);
        suite.addTestSuite(dxc.junit.opcodes.ifne.Test_ifne.class);
        suite.addTestSuite(dxc.junit.opcodes.ifnonnull.Test_ifnonnull.class);
        suite.addTestSuite(dxc.junit.opcodes.ifnull.Test_ifnull.class);
        suite.addTestSuite(dxc.junit.opcodes.iinc.Test_iinc.class);
        suite.addTestSuite(dxc.junit.opcodes.iload.Test_iload.class);
        suite.addTestSuite(dxc.junit.opcodes.iload_0.Test_iload_0.class);
        suite.addTestSuite(dxc.junit.opcodes.iload_1.Test_iload_1.class);
        suite.addTestSuite(dxc.junit.opcodes.iload_2.Test_iload_2.class);
        suite.addTestSuite(dxc.junit.opcodes.iload_3.Test_iload_3.class);
        suite.addTestSuite(dxc.junit.opcodes.imul.Test_imul.class);
        suite.addTestSuite(dxc.junit.opcodes.ineg.Test_ineg.class);
        suite
                .addTestSuite(dxc.junit.opcodes.opc_instanceof.Test_opc_instanceof.class);
        suite
                .addTestSuite(dxc.junit.opcodes.invokeinterface.Test_invokeinterface.class);
        suite
                .addTestSuite(dxc.junit.opcodes.invokespecial.Test_invokespecial.class);
        suite
                .addTestSuite(dxc.junit.opcodes.invokestatic.Test_invokestatic.class);
        suite
                .addTestSuite(dxc.junit.opcodes.invokevirtual.Test_invokevirtual.class);
        suite.addTestSuite(dxc.junit.opcodes.ior.Test_ior.class);
        suite.addTestSuite(dxc.junit.opcodes.irem.Test_irem.class);
        suite.addTestSuite(dxc.junit.opcodes.ireturn.Test_ireturn.class);
        suite.addTestSuite(dxc.junit.opcodes.ishl.Test_ishl.class);
        suite.addTestSuite(dxc.junit.opcodes.ishr.Test_ishr.class);
        suite.addTestSuite(dxc.junit.opcodes.istore.Test_istore.class);
        suite.addTestSuite(dxc.junit.opcodes.istore_0.Test_istore_0.class);
        suite.addTestSuite(dxc.junit.opcodes.istore_1.Test_istore_1.class);
        suite.addTestSuite(dxc.junit.opcodes.istore_2.Test_istore_2.class);
        suite.addTestSuite(dxc.junit.opcodes.istore_3.Test_istore_3.class);
        suite.addTestSuite(dxc.junit.opcodes.isub.Test_isub.class);
        suite.addTestSuite(dxc.junit.opcodes.iushr.Test_iushr.class);
        suite.addTestSuite(dxc.junit.opcodes.ixor.Test_ixor.class);
        suite.addTestSuite(dxc.junit.opcodes.jsr.Test_jsr.class);
        suite.addTestSuite(dxc.junit.opcodes.jsr_w.Test_jsr_w.class);
        suite.addTestSuite(dxc.junit.opcodes.l2d.Test_l2d.class);
        suite.addTestSuite(dxc.junit.opcodes.l2f.Test_l2f.class);
        suite.addTestSuite(dxc.junit.opcodes.l2i.Test_l2i.class);
        suite.addTestSuite(dxc.junit.opcodes.ladd.Test_ladd.class);
        suite.addTestSuite(dxc.junit.opcodes.laload.Test_laload.class);
        suite.addTestSuite(dxc.junit.opcodes.land.Test_land.class);
        suite.addTestSuite(dxc.junit.opcodes.lastore.Test_lastore.class);
        suite.addTestSuite(dxc.junit.opcodes.lcmp.Test_lcmp.class);
        suite.addTestSuite(dxc.junit.opcodes.lconst_0.Test_lconst_0.class);
        suite.addTestSuite(dxc.junit.opcodes.lconst_1.Test_lconst_1.class);
        suite.addTestSuite(dxc.junit.opcodes.ldc.Test_ldc.class);
        suite.addTestSuite(dxc.junit.opcodes.ldc_w.Test_ldc_w.class);
        suite.addTestSuite(dxc.junit.opcodes.ldc2_w.Test_ldc2_w.class);
        suite.addTestSuite(dxc.junit.opcodes.ldiv.Test_ldiv.class);
        suite.addTestSuite(dxc.junit.opcodes.lload.Test_lload.class);
        suite.addTestSuite(dxc.junit.opcodes.lload_0.Test_lload_0.class);
        suite.addTestSuite(dxc.junit.opcodes.lload_1.Test_lload_1.class);
        suite.addTestSuite(dxc.junit.opcodes.lload_2.Test_lload_2.class);
        suite.addTestSuite(dxc.junit.opcodes.lload_3.Test_lload_3.class);
        suite.addTestSuite(dxc.junit.opcodes.lmul.Test_lmul.class);
        suite.addTestSuite(dxc.junit.opcodes.lneg.Test_lneg.class);
        suite
                .addTestSuite(dxc.junit.opcodes.lookupswitch.Test_lookupswitch.class);
        suite.addTestSuite(dxc.junit.opcodes.lor.Test_lor.class);
        suite.addTestSuite(dxc.junit.opcodes.lrem.Test_lrem.class);
        suite.addTestSuite(dxc.junit.opcodes.lreturn.Test_lreturn.class);
        suite.addTestSuite(dxc.junit.opcodes.lshl.Test_lshl.class);
        suite.addTestSuite(dxc.junit.opcodes.lshr.Test_lshr.class);
        suite.addTestSuite(dxc.junit.opcodes.lstore.Test_lstore.class);
        suite.addTestSuite(dxc.junit.opcodes.lstore_0.Test_lstore_0.class);
        suite.addTestSuite(dxc.junit.opcodes.lstore_1.Test_lstore_1.class);
        suite.addTestSuite(dxc.junit.opcodes.lstore_2.Test_lstore_2.class);
        suite.addTestSuite(dxc.junit.opcodes.lstore_3.Test_lstore_3.class);
        suite.addTestSuite(dxc.junit.opcodes.lsub.Test_lsub.class);
        suite.addTestSuite(dxc.junit.opcodes.lushr.Test_lushr.class);
        suite.addTestSuite(dxc.junit.opcodes.lxor.Test_lxor.class);
        suite
                .addTestSuite(dxc.junit.opcodes.monitorenter.Test_monitorenter.class);
        suite
                .addTestSuite(dxc.junit.opcodes.monitorexit.Test_monitorexit.class);
        suite
                .addTestSuite(dxc.junit.opcodes.multianewarray.Test_multianewarray.class);
        suite.addTestSuite(dxc.junit.opcodes.opc_new.Test_opc_new.class);
        suite.addTestSuite(dxc.junit.opcodes.newarray.Test_newarray.class);
        suite.addTestSuite(dxc.junit.opcodes.nop.Test_nop.class);
        suite.addTestSuite(dxc.junit.opcodes.pop.Test_pop.class);
        suite.addTestSuite(dxc.junit.opcodes.pop2.Test_pop2.class);
        suite.addTestSuite(dxc.junit.opcodes.putfield.Test_putfield.class);
        suite.addTestSuite(dxc.junit.opcodes.putstatic.Test_putstatic.class);
        suite.addTestSuite(dxc.junit.opcodes.ret.Test_ret.class);
        suite.addTestSuite(dxc.junit.opcodes.opc_return.Test_opc_return.class);
        suite.addTestSuite(dxc.junit.opcodes.saload.Test_saload.class);
        suite.addTestSuite(dxc.junit.opcodes.sastore.Test_sastore.class);
        suite.addTestSuite(dxc.junit.opcodes.sipush.Test_sipush.class);
        suite.addTestSuite(dxc.junit.opcodes.swap.Test_swap.class);
        suite
                .addTestSuite(dxc.junit.opcodes.tableswitch.Test_tableswitch.class);
        suite.addTestSuite(dxc.junit.opcodes.wide.Test_wide.class);

        return suite;
    }
}
