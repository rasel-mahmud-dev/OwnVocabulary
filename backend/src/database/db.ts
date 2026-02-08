import {CreateIndexesOptions, Db, IndexSpecification, MongoClient, ServerApiVersion} from 'mongodb';
import Diary from "../model/Diary";
import Bookmark from "../model/Bookmark";
import Tag from "../model/Tag";
import DiaryBookmark from "../model/DiaryBookmark";
import DiaryTag from "../model/DiaryTag";
import envConfig from "../config/envConfig";

const uri = envConfig.MONGODB_URI as string;
if (!uri) {
    throw new Error("Please define the MONGODB_URI environment variable.");
}

type MongoDBClient = Db & {
    client?: MongoClient
} | null

let cachedDb: MongoDBClient = null;

async function connectToDatabase() {
    if (cachedDb) {
        return cachedDb;
    }

    const client = new MongoClient(uri, {
        serverApi: {
            version: ServerApiVersion.v1,
            strict: true,
            deprecationErrors: true,
        }
    });

    await client.connect();
    console.log("Connected to MongoDB!");

    cachedDb = client.db("my_vocab_book");
    cachedDb.client = client
    return cachedDb;
}

const collections = [Diary, DiaryTag, DiaryBookmark, Tag, Bookmark]

async function indexed() {
    try {
        const db = await connectToDatabase()
        for (const model of collections) {
            const collection  = db.collection(model.collectionName)
            const indexes = model.indexed() as Array<{idxSpec: IndexSpecification, idxOpt: CreateIndexesOptions}>
            if (!indexes) continue
            for (const item of indexes) {
                try{
                    await collection.createIndex(item.idxSpec, item?.idxOpt ?? {})
                } catch (ex){
                    console.log(`Error creating index for ${model.collectionName}: ${ex?.message}`)
                }
            }
        }
        console.log("Indexes created successfully")
    } catch (ex) {
        console.log("Error creating indexes: ", ex?.message)
    }
}

// indexed()

export default connectToDatabase;
