{
				InputImagePixelVectorType normalizedTargetPatch =
					this->VectorizeImageListPatch(this->m_TargetImage, currentCenterIndex, true);

				absoluteAtlasPatchDifferences.fill(0.0);
				originalAtlasPatchIntensities.fill(0.0);

				// In each atlas, search for a patch that matches the target patch
				for (SizeValueType i = 0; i < this->m_NumberOfAtlases; i++)
				{

					RealType minimumPatchSimilarity = NumericTraits<RealType>::max();
					SizeValueType minimumPatchOffsetIndex = 0;

					for (SizeValueType j = 0; j < searchNeighborhoodSize; j++)
					{
						IndexType searchIndex = currentCenterIndex + searchNeighborhoodOffsetList[j];

						if (!output->GetRequestedRegion().IsInside(searchIndex))
						{
							continue;
						}

						RealType patchSimilarity = this->ComputeNeighborhoodPatchSimilarity(
							this->m_AtlasImages[i], searchIndex, normalizedTargetPatch, useOnlyFirstAtlasImage);

						if (patchSimilarity < minimumPatchSimilarity)
						{
							minimumPatchSimilarity = patchSimilarity;
							minimumPatchOffsetIndex = j;
						}
					}

					// Once the patch has been found, normalize it and then compute the absolute
					// difference with target patch

					IndexType minimumIndex = currentCenterIndex +
						searchNeighborhoodOffsetList[minimumPatchOffsetIndex];
					InputImagePixelVectorType normalizedMinimumAtlasPatch;
					if (numberOfTargetModalities == this->m_NumberOfAtlasModalities)
					{
						normalizedMinimumAtlasPatch =
							this->VectorizeImageListPatch(this->m_AtlasImages[i], minimumIndex, true);
					}
					else
					{
						normalizedMinimumAtlasPatch =
							this->VectorizeImagePatch(this->m_AtlasImages[i][0], minimumIndex, true);
					}

					typename InputImagePixelVectorType::const_iterator itA = normalizedMinimumAtlasPatch.begin();
					typename InputImagePixelVectorType::const_iterator itT = normalizedTargetPatch.begin();
					while (itA != normalizedMinimumAtlasPatch.end())
					{
						RealType value = std::fabs(*itA - *itT);
						absoluteAtlasPatchDifferences(i, itA - normalizedMinimumAtlasPatch.begin()) = value;

						++itA;
						++itT;
					}

					InputImagePixelVectorType originalMinimumAtlasPatch =
						this->VectorizeImageListPatch(this->m_AtlasImages[i], minimumIndex, false);

					typename InputImagePixelVectorType::const_iterator itO = originalMinimumAtlasPatch.begin();
					while (itO != originalMinimumAtlasPatch.end())
					{
						originalAtlasPatchIntensities(i, itO - originalMinimumAtlasPatch.begin()) = *itO;
						++itO;
					}

					minimumAtlasOffsetIndices[i] = minimumPatchOffsetIndex;
				}