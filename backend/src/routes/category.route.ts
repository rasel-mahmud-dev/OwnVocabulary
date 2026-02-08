import { Router } from "express";
const router = Router();

import categoryController from "../controllers/category.controller";
import { checkApiKeys } from "../middlewares";

router.get('/api/v2/category', categoryController.getCategories);
router.put('/api/v2/category/:uid', checkApiKeys, categoryController.updateCategory);
router.get('/api/v2/category/pull', checkApiKeys, categoryController.categoryPull);

export default router
