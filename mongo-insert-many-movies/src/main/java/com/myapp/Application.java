package com.myapp;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

import static com.mongodb.client.model.Filters.eq;


public class Application {
    private static final String MONGO_DB_URL = "mongodb://127.0.0.1:27017,127.0.0.1:27018,127.0.0.1:27019/?replicaSet=rs0";
    private static final String DB_NAME = "online-school";
    private static final double MIN_GPA = 90.0;
    private static final Random random = new Random();
    private static Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        String courseName = args[0];
        String studentName = args[1];
        int age = Integer.parseInt(args[2]);
        double gpa = Double.parseDouble(args[3]);

        MongoDatabase onlineSchoolDb = connectToMongoDB(MONGO_DB_URL, DB_NAME);
        enroll(onlineSchoolDb, courseName, studentName, age, gpa);
    }

    private static void enroll(MongoDatabase database, String courseName, String studentName, int age, double gpa) {
        if (!isValidCourse(database, courseName)) {
            logger.error("Invalid course: {}", courseName);
            return;
        }

        MongoCollection<Document> courseCollection = database.getCollection(courseName)
                .withWriteConcern(WriteConcern.MAJORITY)
                .withReadPreference(ReadPreference.primaryPreferred());

        if (courseCollection.find(eq("name", studentName)).first() != null) {
            logger.warn("Student: {} already enrolled", studentName);
            return;
        }

        if (gpa < MIN_GPA) {
            logger.warn("Please improve your grades");
            return;
        }

        courseCollection.insertOne(new Document("name", studentName).append("age", age).append("gpa", gpa));

        logger.info("Student {} was successfully enrolled in {}", studentName, courseName);

        for (Document document : courseCollection.find()) {
            logger.info(document.toJson());
        }
    }

    private static boolean isValidCourse(MongoDatabase database, String courseName) {
        for (String name : database.listCollectionNames()) {
            if (name.equals(courseName)) {
                return true;
            }
        }
        return false;
    }

    private static MongoDatabase connectToMongoDB(String url, String dbName) {
        MongoClient mongoClient = new MongoClient(new MongoClientURI(url));
        return mongoClient.getDatabase(dbName);
    }


}

