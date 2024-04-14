package com.km.commentservice.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import com.km.commentservice.dto.NestedCommentReply;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;


/**
 * @author karanm
 */
public class TestUtils {

    private static final String TEST_RESOURCE_PATH = "src/test/resources/testing";

    public static String getFileContents(String name) {
        String content;
        try (InputStream inputStream = new ClassPathResource(name).getInputStream()) {
            content = StreamUtils.copyToString(inputStream, Charset.forName("UTF-8"));
        } catch (Exception e) {
            throw new RuntimeException("Error in reading file " + name, e);
        }
        return content;
    }

    public static void writeToTestResource(String content, String name) throws IOException {
        writeToFile(content, TEST_RESOURCE_PATH + name);
    }

    public static void writeToFile(String content, String name) throws IOException {
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(new File(name));

            outputStream.write(content.getBytes());
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }

    public static Boolean assertNestedReplyEqual(NestedCommentReply expected, NestedCommentReply actual) {
        if (expected.getCommentReply() != null && actual.getCommentReply() != null) {
            if (!expected.getCommentReply().equals(actual.getCommentReply())) {
                return false;
            }
        } else if (expected.getCommentReply() != null || actual.getCommentReply() != null) {
            return false;
        }
        if (expected.getNestedCommentReplies().size() != actual.getNestedCommentReplies().size()) {
            return false;
        }
        for (int i = 0; i < expected.getNestedCommentReplies().size(); i++) {
            if (!assertNestedReplyEqual(expected.getNestedCommentReplies().get(i), actual.getNestedCommentReplies().get(i))) {
                return false;
            }
        }
        return true;
    }
}
