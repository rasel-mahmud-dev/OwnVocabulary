import {Collection, Filter, UpdateFilter, UpdateOptions, WithId} from "mongodb";
import connectToDatabase from "../database/db";

class RepositoryBase {
    static async findAll(collectionName: string, ...rest: any) {
        const db = await connectToDatabase()
        const collection = db.collection(collectionName)
        // @ts-ignore
        return collection.find(...rest).toArray()

    }

    static async count(collectionName: string, ...rest: any) {
        const db = await connectToDatabase()
        const collection = db.collection(collectionName)
        return collection.countDocuments(...rest)
    }

    static async aggregate(collectionName: string, pipeline: Array<any>) {
        const db = await connectToDatabase()
        const collection = db.collection(collectionName)
        return collection.aggregate(pipeline).toArray()
    }

    static async findById(collectionName: string, filter: any) {
        const db = await connectToDatabase()
        const collection = db.collection(collectionName)
        return collection.findOne(filter)
    }


    static async findOne<T>(collectionName: string, filter: any, otp = {}) {
        const db = await connectToDatabase()
        const collection = db.collection(collectionName)
        return await collection.findOne(filter, otp) as T
    }

    static async insert<T>(collectionName: string, data: T) {
        const db = await connectToDatabase();
        const collection: Collection<WithId<T>> = db.collection(collectionName);
        const result = await collection.insertOne(data as any);
        return {
            ...data,
            _id: result?.insertedId,
        } as T
    }

    static async insertMany<T>(collectionName: string, data: T[]) {
        const db = await connectToDatabase();
        const collection: Collection<WithId<T>> = db.collection(collectionName);
        const result = await collection.insertMany(data as any);
        return result
    }

    static async deleteOne(collectionName: string, filter: any) {
        const db = await connectToDatabase();
        const collection = db.collection(collectionName);
        const result = await collection.deleteOne(filter);
        return result;
    }

    static async deleteMany(collectionName: string, filter: any) {
        const db = await connectToDatabase();
        const collection = db.collection(collectionName);
        const result = await collection.deleteMany(filter);
        return result;
    }

    static async updateOne(collectionName: string, filter: any, data) {
        const db = await connectToDatabase();
        const collection = db.collection(collectionName);
        const result = await collection.updateOne(filter, {$set: data});
        return result;
    }
    static async updateMany(collectionName: string, filter: any, data) {
        const db = await connectToDatabase();
        const collection = db.collection(collectionName);
        const result = await collection.updateMany(filter, {$set: data});
        return result;
    }

    static async updateOneWithUpsert(collectionName: string, filter: any, data) {
        const db = await connectToDatabase();
        const collection = db.collection(collectionName);
        const result = await collection.updateOne(filter, {$set: data}, {upsert: true});
        return result;
    }

    static async updateOneWithRaw<T extends Document>(collectionName: string, filter: Filter<T>, update: UpdateFilter<T> | Partial<T>, options?: UpdateOptions) {
        const db = await connectToDatabase();
        const collection = db.collection(collectionName);
        const result = await collection.updateOne(filter, update, options);
        return result;
    }

    static async updateWithUpsert(collectionName: string, filter: any, data: any) {
        const db = await connectToDatabase();
        const collection = db.collection(collectionName);
        const result = await collection.updateOne(filter, data, {upsert: true});
        return result;
    }
}

export default RepositoryBase
