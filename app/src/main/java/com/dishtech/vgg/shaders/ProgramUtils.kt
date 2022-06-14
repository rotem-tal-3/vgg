package com.dishtech.vgg.shaders

import android.opengl.GLES30
import android.opengl.GLES32
import android.opengl.GLES32.GL_STREAM_READ
import android.opengl.GLES32.GL_TEXTURE_BUFFER
import android.opengl.GLU.gluErrorString
import java.nio.ByteBuffer
import java.nio.ByteOrder

object ProgramUtils {

    private const val UNKNOWN_PROGRAM = -1
    /**
     * Checks for a GL error. Throws A runtime exception if an error occurred.
     */
    fun checkGlError(op: String) {
        val error = GLES30.glGetError();
        if (error != GLES30.GL_NO_ERROR) {
            throw RuntimeException("$op: glError $error\n${gluErrorString(error)}")
        }
    }
//    fun GetFirstNMessages(numMsgs: Int) {
//        val maxMsgLen = IntArray(1)
//        GLES30.glGetIntegerv(GLES32.GL_MAX_DEBUG_MESSAGE_LENGTH, maxMsgLen, 0);
//        val msgData = CharArray(numMsgs * maxMsgLen[0])
//        val sources = IntArray(numMsgs)
//        val types = IntArray(numMsgs)
//        val severities = IntArray(numMsgs);
//        val ids = IntArray(numMsgs);
//        val lengths = IntArray(numMsgs);
//
//        val numFound = GLES32.glGetDebugMessageLog(numMsgs, sources, 0, types, 0 ,ids, 0,
//                                                   severities, 0)
//
//        sources.resize(numFound);
//        types.resize(numFound);
//        severities.resize(numFound);
//        ids.resize(numFound);
//        lengths.resize(numFound);
//
//        std::vector<std::string> messages;
//        messages.reserve(numFound);
//
//        std::vector<GLchar>::iterator currPos = msgData.begin();
//        for(size_t msg = 0; msg < lengths.size(); ++msg)
//        {
//            messages.push_back(std::string(currPos, currPos + lengths[msg] - 1));
//            currPos = currPos + lengths[msg];
//        }
//    }

    /**
     * Deletes a program if it exists.
     */
    fun deleteIfNeeded(program: Int) {
        if (program != UNKNOWN_PROGRAM) {
            GLES30.glDeleteProgram(program)
        }
    }
}